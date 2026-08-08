import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { fetchJobs, uploadJobs, type JobUploadRequest } from './api.ts'
import { mockJob, mockPage } from './test/fixtures.ts'

describe('fetchJobs', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('requests /api/jobs with no query string when no params are given', async () => {
    const page = mockPage()
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify(page)))

    const result = await fetchJobs()

    expect(fetch).toHaveBeenCalledWith('/api/jobs')
    expect(result).toEqual(page)
  })

  it('includes title, country, sort, page, and size in the query string', async () => {
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify(mockPage())))

    await fetchJobs({
      title: 'Engineer',
      country: 'USA',
      sortBy: 'salary',
      sortDirection: 'desc',
      page: 2,
      size: 10,
    })

    const url = vi.mocked(fetch).mock.calls[0][0] as string
    const params = new URLSearchParams(url.split('?')[1])
    expect(params.get('title')).toBe('Engineer')
    expect(params.get('country')).toBe('USA')
    expect(params.get('sortBy')).toBe('salary')
    expect(params.get('sortDirection')).toBe('desc')
    expect(params.get('page')).toBe('2')
    expect(params.get('size')).toBe('10')
  })

  it('omits sortDirection when sortBy is not set', async () => {
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify(mockPage())))

    await fetchJobs({ sortDirection: 'desc' })

    const url = vi.mocked(fetch).mock.calls[0][0] as string
    expect(url).not.toContain('sortDirection')
  })

  it('throws with status details when the response is not ok', async () => {
    vi.mocked(fetch).mockResolvedValue(new Response('', { status: 500, statusText: 'Internal Server Error' }))

    await expect(fetchJobs()).rejects.toThrow('Failed to fetch jobs: 500 Internal Server Error')
  })
})

describe('uploadJobs', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  const request: JobUploadRequest = {
    title: 'Software Engineer',
    company: 'Acme Corp',
    location: 'USA',
    salary: 150000,
    employment_type: 'Full-Time',
    posting_date: '2026-08-01',
    company_type: 'Direct Employer'
  }

  it('posts JSON to /api/jobs/upload and returns the parsed results', async () => {
    const results = [{ result: 'success' as const, job: mockJob(), error: null }]
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify(results)))

    const result = await uploadJobs([request])

    expect(fetch).toHaveBeenCalledWith('/api/jobs/upload', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify([request]),
    })
    expect(result).toEqual(results)
  })

  it('throws with status details when the response is not ok', async () => {
    vi.mocked(fetch).mockResolvedValue(new Response('', { status: 400, statusText: 'Bad Request' }))

    await expect(uploadJobs([request])).rejects.toThrow('Failed to upload jobs: 400 Bad Request')
  })
})
