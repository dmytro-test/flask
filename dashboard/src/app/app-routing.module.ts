import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'pipelines',
    pathMatch: 'full'
  },
  {
    path: 'pipelines',
    loadChildren: () => import('./pipelines/pipelines.module').then(mod => mod.PipelinesModule)
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
