import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PipelinesStateService } from '../../../state/pipelines-state.service';

@Component({
  selector: 'app-pipeline-details',
  templateUrl: './pipeline-details.component.html',
  styleUrls: ['./pipeline-details.component.scss']
})
export class PipelineDetailsComponent implements OnInit {
  public nodes: any[];
  public links: any[];
  public description: any;

  constructor(private pipelinesStateService: PipelinesStateService, private router: Router, private activatedRoute: ActivatedRoute) {}

  async ngOnInit(): Promise<void> {
    const pipelineId = this.activatedRoute.snapshot.paramMap.get('id');
    if (!!pipelineId) {
      const pipelineDetails = await this.pipelinesStateService.getPipelineDetails(pipelineId);
      this.nodes = [...pipelineDetails.nodes];
      this.links = [...pipelineDetails.links];
      this.description = pipelineDetails.description
    } else {
      this.router.navigateByUrl('/');
    }
  }
}
