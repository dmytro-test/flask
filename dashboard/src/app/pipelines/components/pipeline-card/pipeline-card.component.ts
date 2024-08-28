import { Component, Input, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-pipeline-card',
  templateUrl: './pipeline-card.component.html',
  styleUrls: ['./pipeline-card.component.scss']
})
export class PipelineCardComponent {
  @Input() id: string;
  @Input() title: string;
  @Input() description: string;
  @Input() status: 'GOOD' | 'BAD';
  @Output() pipelineClicked = new EventEmitter<string>();

  constructor() {}
}
