import { Injectable } from '@angular/core';
import { PipelinesHttpActionsService } from '../services/pipelines-http-actions.service';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class PipelinesStateService {
  private readonly _pipelines: BehaviorSubject<any> = new BehaviorSubject<any>(null);

  public pipelines$ = this._pipelines.asObservable();

  constructor(private pipelinesHttpActionsService: PipelinesHttpActionsService) {}

  public getPipelines(): Observable<any> {
    return this.pipelinesHttpActionsService.getPipelines().pipe(
      map((pipelines: any[]) => {
        this._pipelines.next([...pipelines]);
      })
    );
  }

  public async getPipelineDetails(pipelineId: string): Promise<any> {
    return await this.pipelinesHttpActionsService.getPipelineDetails(pipelineId).toPromise();
  }
}
