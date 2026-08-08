import type { Job, JobUploadResult, Page } from '../api.ts'

export function mockJob(overrides: Partial<Job> = {}): Job {
  return {
    id: 1,
    title: 'Software Engineer',
    description: 'Build things.',
    company: 'Acme Corp',
    location: { id: 1, city: 'New York', state: 'NY', country: 'USA' },
    salaryValue: 150000,
    salaryCurrency: 'USD',
    salaryPeriod: 'Annual',
    employmentType: 'Full-Time',
    postingDate: '2026-08-01',
    companyType: 'Direct Employer',
    language: 'English',
    remote: false,
    approved: true,
    rejectionReason: null,
    ...overrides,
  }
}

export function mockPage(overrides: Partial<Page<Job>> = {}): Page<Job> {
  return {
    content: [mockJob()],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 20,
    first: true,
    last: true,
    ...overrides,
  }
}

export function mockUploadResult(overrides: Partial<JobUploadResult> = {}): JobUploadResult {
  return {
    result: 'success',
    job: mockJob(),
    error: null,
    ...overrides,
  }
}
