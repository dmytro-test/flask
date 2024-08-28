import { AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { drag, forceCenter, forceCollide, forceLink, forceManyBody, forceSimulation, select, zoom, zoomIdentity } from 'd3';
import { CONFIG, SELECTORS, NODE_TYPES, LINK_TYPES, NODE_RISK_TYPES } from './consts';

@Component({
  selector: 'app-graph',
  templateUrl: './graph.component.html',
  styleUrls: ['./graph.component.scss']
})
export class GraphComponent implements OnInit, AfterViewInit {
  @Input() nodes: any[];
  @Input() links: any[];

  public svg: any;
  public g: any;
  public simulation: any;
  public allNodesSelection: any;
  public nodesSelection: any;
  public allLinksSelection: any;
  public zoomHandler: any;

  @ViewChild('svgContainer', { read: ElementRef, static: false }) svgContainer: ElementRef;

  private tickTransition = false;
  private fixedRoot = true;

  private currentZoomScale = 1;
  private minZoomScale = 0.7;
  private zoomInitialState = true;

  private readonly PIPELINE_GRAPH_SVG_CLASS = '.pipeline-graph-svg';
  private readonly PIPELINE_GRAPH_CONTAINER_CLASS = 'pipeline-graph-container';

  constructor() {}

  public ngOnInit(): void {}

  public ngAfterViewInit(): void {
    this.initSvg();
    this.initForceSimulation();
    this.initLinks();
    this.initNodes();
    this.initZoomHandler();
    this.initSimulationEventHandler();
    this.setSvgCenter(this.currentZoomScale < this.minZoomScale ? this.minZoomScale : this.currentZoomScale);
  }

  private getWidth(): number {
    return this.svgContainer.nativeElement.offsetWidth;
  }
  private getHeight(): number {
    return this.svgContainer.nativeElement.offsetHeight;
  }

  private initSvg(): void {
    this.svg = select(this.PIPELINE_GRAPH_SVG_CLASS);
    this.g = this.svg.append('g').attr('class', this.PIPELINE_GRAPH_CONTAINER_CLASS);
  }

  private initForceSimulation(): void {
    // const classes = this.nodes.filter((n: any) => n.nodeType.toUpperCase() === NODE_TYPES.CLASS);

    this.simulation = forceSimulation()
      .force(
        'link',
        forceLink()
          .id(d => {
            return (d as any).id;
          })
          // .strength((l: any) => {
          //   let result = CONFIG.SIMULATION.LINK_STRENGTH.DEFAULT;
          //   switch (l.source.nodeType.toUpperCase()) {
          //     case NODE_TYPES.ROOT:
          //       result = classes.length > 0 ? CONFIG.SIMULATION.LINK_STRENGTH.ROOT : CONFIG.SIMULATION.LINK_STRENGTH.CLASS;
          //       break;
          //     case NODE_TYPES.CLASS:
          //       result =
          //         this.nodes.length < CONFIG.SIMULATION.NODES_THRESHOLD
          //           ? CONFIG.SIMULATION.LINK_STRENGTH.ROOT
          //           : CONFIG.SIMULATION.LINK_STRENGTH.CLASS;
          //       break;
          //     case NODE_TYPES.METHOD:
          //       result =
          //         this.nodes.length < CONFIG.SIMULATION.NODES_THRESHOLD
          //           ? CONFIG.SIMULATION.LINK_STRENGTH.SERVICE
          //           : CONFIG.SIMULATION.LINK_STRENGTH.DEFAULT;
          //       break;
          //   }
          //   return result;
          // })
          .distance((l: any) => {
            let result: number;
            switch (l.source.nodeType.toUpperCase()) {
              case NODE_TYPES.ROOT:
                result = 150;
                // result =
                //   classes.length > 0 && this.nodes.length < CONFIG.SIMULATION.NODES_THRESHOLD
                //     ? CONFIG.SIMULATION.LINK_DISTANCE.CLASS
                //     : CONFIG.SIMULATION.LINK_DISTANCE.ROOT;
                break;
              case NODE_TYPES.CLASS:
                result = 130;
                break;

              case NODE_TYPES.METHOD:
                result = 100;
                break;
              default:
                result =
                  this.nodes.length < CONFIG.SIMULATION.NODES_THRESHOLD
                    ? CONFIG.SIMULATION.LINK_DISTANCE.DEFAULT
                    : CONFIG.SIMULATION.LINK_DISTANCE.ROOT;
                break;
            }
            return result;
          })
      )
      // .alpha(CONFIG.SIMULATION.ALPHA)
      // .alphaDecay(CONFIG.SIMULATION.ALPHA_DECAY)
      // .alphaTarget(CONFIG.SIMULATION.ALPHA_TARGET)
      .force(
        'charge',
        forceManyBody().strength(
          this.nodes.length < CONFIG.SIMULATION.NODES_THRESHOLD
            ? CONFIG.SIMULATION.CHARGE_FORCE.MIN_STRENGTH
            : CONFIG.SIMULATION.CHARGE_FORCE.MAX_STRENGTH
        )
      )
      .force('collide', forceCollide().radius(CONFIG.NODE.DEFAULT.RADIUS));

    this.setSimulationCenter(false);
  }

  private initLinks(): void {
    this.enrichLinks(this.links);

    this.allLinksSelection = this.g
      .selectAll('line')
      .data(this.links)
      .enter()
      .append('path')
      .attr('class', CONFIG.LINK.CLASS_NAME)
      .attr('opacity', CONFIG.LINK.OPACITY)
      .attr('fill', CONFIG.LINK.FILL)
      .attr('stroke', (d: any) => CONFIG.LINK.STROKE[d.type])
      .attr('stroke-width', (d: any) => CONFIG.LINK.STROKE_WIDTH[d.type])
      // .attr('stroke-dasharray', (d: any) => {
      //   if (d.type && d.type === 'unused') {
      //     return CONFIG.LINK.DASH;
      //   }
      //   return '0';
      // })
      .style('pointer-events', 'none');
  }

  private enrichLinks(links: any[]): void {
    links.map((link: any) => {
      const sameLinks = links.filter((sameLink: any) => sameLink.source === link.source && sameLink.target === link.target);
      sameLinks.map((s: any, i) => {
        s.sameIndex = i + 1;
        s.sameTotal = sameLinks.length;
        s.sameTotalHalf = sameLinks.length / 2;
        s.sameUneven = sameLinks.length % 2 !== 0;
        s.sameMiddleLink = s.sameUneven === true && Math.ceil(s.sameTotalHalf) === s.sameIndex;
        s.sameLowerHalf = s.sameIndex <= s.sameTotalHalf;
        s.sameArcDirection = s.sameLowerHalf ? 0 : 1;
        s.sameIndexCorrected = s.sameLowerHalf ? s.sameIndex : s.sameIndex - Math.ceil(s.sameTotalHalf);
        s.maxSameHalf = 1;
        link = { ...link, ...s };
      });
    });
  }

  private initNodes(): void {
    this.allNodesSelection = this.createNodes();

    this.addNodes();
  }

  private initZoomHandler(): void {
    if (this.zoomInitialState) {
      this.currentZoomScale = this.minZoomScale;
      this.zoomInitialState = false;
    }
    this.zoomHandler = zoom()
      .scaleExtent([this.minZoomScale, CONFIG.ZOOM.MAX])
      // d3Event is ZoomEvent, for some reason I couldn't import it
      .on('zoom', d3Event => {
        this.currentZoomScale = d3Event.transform.k;
        this.g.attr('transform', d3Event.transform);
      });

    this.svg.call(this.zoomHandler).on('dblclick.zoom', null);
  }

  private initSimulationEventHandler(): void {
    const nodesWithLinks = [...this.nodes, ...this.links];
    this.simulation
      .nodes(this.nodes.length < CONFIG.NODE.MIN_NODES_LENGTH ? nodesWithLinks : this.nodes)
      .on('tick', () => this.simulationTick());
    this.simulation.force('link').links(this.links);
  }

  // private onNodeClick(d3Event: any, d: any): void {
  //   d3Event.stopPropagation();
  // }

  private simulationTick(): void {
    // if (this.fixedRoot) {
    //   const root = this.nodes.filter(d => d.nodeType.toUpperCase() === NODE_TYPES.ROOT);
    //   if (root.length > 0) {
    //     (root[0] as any).fx = this.getWidth() / 2;
    //     (root[0] as any).fy = this.getHeight() / 2;
    //   }
    //
    //   this.fixedRoot = false;
    // }
    if (this.tickTransition === false) {
      this.allLinksSelection.attr('d', d => this.linkArc(d));
      this.allNodesSelection.attr('transform', d => 'translate(' + d.x + ',' + d.y + ')');
    } else {
      this.tickTransition = false;
      this.allLinksSelection
        .transition()
        .duration(CONFIG.SIMULATION.TICK_DURATION)
        .attr('d', d => this.linkArc(d));
      this.allNodesSelection
        .transition()
        .duration(CONFIG.SIMULATION.TICK_DURATION)
        .attr('transform', d => 'translate(' + d.x + ',' + d.y + ')');
    }
  }

  private linkArc(link: any): string {
    const dx = link.target.x - link.source.x;
    const dy = link.target.y - link.source.y;
    const dr = Math.sqrt(dx * dx + dy * dy) * 5;
    const unevenCorrection = link.sameUneven ? 0 : 0.5;
    let arc = (dr * link.maxSameHalf) / (link.sameIndexCorrected - unevenCorrection);

    if (link.sameMiddleLink) {
      arc = 0;
    }

    return (
      'M' +
      link.source.x +
      ',' +
      link.source.y +
      'A' +
      arc +
      ',' +
      arc +
      ' 0 0,' +
      link.sameArcDirection +
      ' ' +
      link.target.x +
      ',' +
      link.target.y
    );
  }

  private setSimulationCenter(restart: boolean): void {
    const width = this.getWidth();
    const height = this.getHeight();

    if (restart) {
      this.allNodesSelection.each(d => {
        d.fx = null;
        d.fy = null;
      });
      this.tickTransition = true;
      this.simulation.restart();
      this.setSvgCenter(this.currentZoomScale);
    }

    this.simulation.force('center', forceCenter(width / 2, height / 2));
  }

  private setSvgCenter(scale: number): void {
    const width = this.getWidth();
    const height = this.getHeight();
    this.currentZoomScale = scale;

    this.svg
      .transition()
      .duration(250)
      .call(
        this.zoomHandler.transform,
        zoomIdentity
          .translate(width / 2, height / 2)
          .scale(scale)
          .translate(-width / 2, -height / 2)
      );
  }

  private createNodes(): any {
    return (
      this.g
        .selectAll(`.${CONFIG.NODE.CLASS_NAME}`)
        .data(this.nodes)
        .enter()
        .append('g')
        .attr('class', CONFIG.NODE.G_CLASS_NAME)
        .call(
          drag()
            .on('start', (d3Event, d) => this.dragStarted(d3Event))
            .on('drag', (d3Event, d) => this.dragging(d3Event, d))
            .on('end', (d3Event, d) => this.dragEnded(d3Event))
        )
        // .on('click', (d3Event: any, d: any) => this.onNodeClick(d3Event, d))
        .style('cursor', 'pointer')
    );
  }

  private addNodes(): void {
    this.nodesSelection = this.allNodesSelection.attr('opacity', 1).attr('pointer-events', null);

    const nodesContainer = this.nodesSelection
      .append('g')
      .attr('class', 'large')
      .attr('opacity', 1)
      .attr('pointer-events', null);

    this.addNode(nodesContainer, CONFIG.NODE.CLASS_NAME, CONFIG.NODE.ROOT.RADIUS, CONFIG.NODE.DEFAULT.RADIUS);
    this.addNodeText(nodesContainer, SELECTORS.TEXT);
  }

  private addNodeText(node: any, className: string): void {
    node
      .append('text')
      .attr('class', className)
      .attr('dy', d => (d.nodeType.toUpperCase() === NODE_TYPES.ROOT ? CONFIG.NODE.ROOT.TEXT_DY : CONFIG.NODE.DEFAULT.TEXT_DY))
      .attr('text-anchor', CONFIG.NODE.DEFAULT.TEXT_ANCHOR)
      .style('font-size', CONFIG.NODE.DEFAULT.FONT_SIZE)
      .style('pointer-events', 'none')
      .text(d => {
        const result = d?.displayName?.length > 0 ? d.displayName : d.name;
        return result.length > CONFIG.NODE.DEFAULT.MAX_TEXT_LENGTH
          ? result.substring(0, CONFIG.NODE.DEFAULT.MAX_TEXT_LENGTH) + '...'
          : result;
      });
  }

  private addNode(
    container: any,
    className: string,
    rootRadius: number,
    radius: number
    // mouseOverCallback: (d: any, i: number, svg: SVGElement) => void,
    // mouseOutCallback: () => void
  ): void {
    container
      .append('circle')
      .attr('fill', 'white')
      .attr('stroke', (d: any) => CONFIG.NODE.RISK_TYPE[d.riskType])

      // .attr('class', d => {
      //   switch (d.nodeType.toUpperCase()) {
      //     case NODE_TYPES.ROOT:
      //       return `${className} root-node`;
      //     case NODE_TYPES.CLASS:
      //       return `${className} class`;
      //     case NODE_TYPES.METHOD:
      //       return `${className} method`;
      //     case NODE_TYPES.ACTION:
      //       return `${className} action`;
      //   }
      // })
      .attr('stroke-width', d =>
        d.nodeType.toUpperCase() === NODE_TYPES.ROOT ? CONFIG.NODE.ROOT.STROKE_WIDTH : CONFIG.NODE.DEFAULT.STROKE_WIDTH
      )
      .attr('r', d => (d.nodeType.toUpperCase() === NODE_TYPES.ROOT ? rootRadius : radius));
    // .on('mouseover', mouseOverCallback)
    // .on('mouseout', mouseOutCallback);
  }

  private dragStarted(d3Event: any): void {
    if (!d3Event.active) {
      this.simulation.alphaTarget(CONFIG.SIMULATION.DRAG_ALPHA_TARGET).restart();
    }
  }

  private dragging(d3Event: any, d): void {
    d.fx = d3Event.x;
    d.fy = d3Event.y;
  }

  private dragEnded(d3Event: any): void {
    if (!d3Event.active) {
      this.simulation.alphaTarget(CONFIG.SIMULATION.ALPHA_TARGET);
    }
  }
}
