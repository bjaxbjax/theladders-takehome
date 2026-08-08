export interface Location {
  id: number
  city: string | null
  state: string | null
  country: string
}

export type SalaryPeriod = 'Annual' | 'Hourly'
export type EmploymentType = 'Full-Time' | 'Internship' | 'Contract' | 'Part-Time'
export type CompanyType = 'Direct Employer' | 'Staffing Firm' | 'Consulting Agency'

export interface Job {
  id: number
  title: string
  description: string | null
  company: string
  location: Location
  salaryValue: number
  salaryCurrency: string
  salaryPeriod: SalaryPeriod
  employmentType: EmploymentType
  postingDate: string
  companyType: CompanyType
  language: string
  remote: boolean
  approved: boolean
  rejectionReason: string | null
}

export type SortBy = 'salary' | 'postingDate'
export type SortDirection = 'asc' | 'desc'

export interface JobSearchParams {
  title?: string
  country?: string
  sortBy?: SortBy
  sortDirection?: SortDirection
  page?: number
  size?: number
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

export async function fetchJobs(params: JobSearchParams = {}): Promise<Page<Job>> {
  const query = new URLSearchParams()
  if (params.title) query.set('title', params.title)
  if (params.country) query.set('country', params.country)
  if (params.sortBy) query.set('sortBy', params.sortBy)
  if (params.sortBy && params.sortDirection) query.set('sortDirection', params.sortDirection)
  if (params.page !== undefined) query.set('page', String(params.page))
  if (params.size !== undefined) query.set('size', String(params.size))

  const queryString = query.toString()
  const response = await fetch(`/api/jobs${queryString ? `?${queryString}` : ''}`)

  if (!response.ok) {
    throw new Error(`Failed to fetch jobs: ${response.status} ${response.statusText}`)
  }

  return response.json()
}

export interface JobUploadRequest {
  title: string
  description?: string | null
  company: string
  location: { city?: string | null; state?: string | null; country: string } | string
  salary: { value: number; currency: string; unit?: SalaryPeriod } | number
  employment_type: EmploymentType
  posting_date: string
  company_type: CompanyType
  language?: string | null
  remote?: boolean
}

export interface JobUploadResult {
  result: 'success' | 'error'
  job: JobUploadRequest | Job
  error: string | null
}

export async function uploadJobs(jobs: JobUploadRequest[]): Promise<JobUploadResult[]> {
  const response = await fetch('/api/jobs/upload', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(jobs),
  })

  if (!response.ok) {
    throw new Error(`Failed to upload jobs: ${response.status} ${response.statusText}`)
  }

  return response.json()
}
