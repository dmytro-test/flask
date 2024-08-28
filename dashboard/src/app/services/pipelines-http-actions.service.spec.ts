import { TestBed } from '@angular/core/testing';

import { PipelinesHttpActionsService } from './pipelines-http-actions.service';

describe('PipelinesHttpActionsService', () => {
  let service: PipelinesHttpActionsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PipelinesHttpActionsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
