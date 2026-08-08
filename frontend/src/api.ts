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
}

export type SortBy = 'salary' | 'postingDate'
export type SortDirection = 'asc' | 'desc'

export interface JobSearchParams {
  title?: string
  country?: string
  sortBy?: SortBy
  sortDirection?: SortDirection
}

export async function fetchJobs(params: JobSearchParams = {}): Promise<Job[]> {
  const query = new URLSearchParams()
  if (params.title) query.set('title', params.title)
  if (params.country) query.set('country', params.country)
  if (params.sortBy) query.set('sortBy', params.sortBy)
  if (params.sortBy && params.sortDirection) query.set('sortDirection', params.sortDirection)

  const queryString = query.toString()
  const response = await fetch(`/api/jobs${queryString ? `?${queryString}` : ''}`)

  if (!response.ok) {
    throw new Error(`Failed to fetch jobs: ${response.status} ${response.statusText}`)
  }

  return response.json()
}
