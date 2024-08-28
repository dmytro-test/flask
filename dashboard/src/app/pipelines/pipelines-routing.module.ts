import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { PipelinesComponent } from './pipelines.component';
import { PipelineDetailsComponent } from './components/pipeline-details/pipeline-details.component';

const routes: Routes = [
  { path: '', component: PipelinesComponent },
  { path: ':id', component: PipelineDetailsComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PipelinesRoutingModule {}
