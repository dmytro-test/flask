import { TestBed } from '@angular/core/testing';

import { PipelinesStateService } from './pipelines-state.service';

describe('PipelinesStateService', () => {
  let service: PipelinesStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PipelinesStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
