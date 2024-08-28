import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
// @ts-ignore
import * as pipelinesData from '../../assets/mock-data/pipelines-data.json';

@Injectable({
  providedIn: 'root'
})
export class PipelinesHttpActionsService {
  constructor(private readonly http: HttpClient) {}

  public getPipelines(): Observable<any[]> {
    return of(pipelinesData['pipelines']);
  }

  public getPipelineDetails(pipelineId: string): Observable<any[]> {
    return of(pipelinesData['pipelines-details'][pipelineId]);
  }
}
