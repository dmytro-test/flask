import { Component, OnDestroy, OnInit } from '@angular/core';
import { PipelinesStateService } from '../state/pipelines-state.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-pipelines',
  templateUrl: './pipelines.component.html',
  styleUrls: ['./pipelines.component.scss']
})
export class PipelinesComponent implements OnInit, OnDestroy {
  public pipelines$ = this.pipelinesStateService.pipelines$;

  private subscriptions = new Subscription();

  constructor(private pipelinesStateService: PipelinesStateService, private router: Router, private activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.subscriptions.add(this.pipelinesStateService.getPipelines().subscribe(() => {}));
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  public pipelineClicked(id: string): void {
    this.router.navigate([`${id}`], { relativeTo: this.activatedRoute });
  }
}
