import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PipelinesComponent } from './pipelines.component';
import { PipelinesRoutingModule } from './pipelines-routing.module';
import { MatCardModule } from '@angular/material/card';
import { PipelineCardComponent } from './components/pipeline-card/pipeline-card.component';
import { PipelineDetailsComponent } from './components/pipeline-details/pipeline-details.component';
import { GraphComponent } from './components/pipeline-details/components/graph/graph.component';

@NgModule({
  declarations: [PipelinesComponent, PipelineCardComponent, PipelineDetailsComponent, GraphComponent],
  imports: [CommonModule, PipelinesRoutingModule, MatCardModule]
})
export class PipelinesModule {}
