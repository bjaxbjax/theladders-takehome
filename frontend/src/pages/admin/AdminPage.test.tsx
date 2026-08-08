import { beforeEach, describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AdminPage from './AdminPage.tsx'
import { uploadJobs, type JobUploadResult } from '../../api.ts'
import { mockJob, mockUploadResult } from '../../test/fixtures.ts'

vi.mock('../../api.ts', () => ({
  uploadJobs: vi.fn(),
}))

function fillJson(text: string) {
  fireEvent.change(screen.getByLabelText('Jobs JSON'), { target: { value: text } })
}

describe('AdminPage', () => {
  beforeEach(() => {
    vi.mocked(uploadJobs).mockReset()
  })

  it('renders an empty form with no status or results initially', () => {
    render(<AdminPage />)

    expect(screen.getByLabelText('Jobs JSON')).toHaveValue('')
    expect(screen.getByRole('button', { name: 'Upload jobs' })).toBeEnabled()
    expect(screen.queryByRole('status')).not.toBeInTheDocument()
    expect(screen.queryAllByRole('listitem')).toHaveLength(0)
  })

  it('shows an error and does not call uploadJobs when the input is not valid JSON', async () => {
    const user = userEvent.setup()
    render(<AdminPage />)

    fillJson('{not valid json')
    await user.click(screen.getByRole('button', { name: 'Upload jobs' }))

    expect(screen.getByRole('status')).toHaveTextContent('Invalid JSON:')
    expect(uploadJobs).not.toHaveBeenCalled()
  })

  it('shows an error and does not call uploadJobs when the JSON is not an array', async () => {
    const user = userEvent.setup()
    render(<AdminPage />)

    fillJson('{"title": "Software Engineer"}')
    await user.click(screen.getByRole('button', { name: 'Upload jobs' }))

    expect(screen.getByRole('status')).toHaveTextContent('JSON must be an array of job objects.')
    expect(uploadJobs).not.toHaveBeenCalled()
  })

  it('shows Uploading… and disables the button while the request is in flight', async () => {
    let resolveUpload: (value: JobUploadResult[]) => void = () => {}
    vi.mocked(uploadJobs).mockReturnValue(
      new Promise((resolve) => {
        resolveUpload = resolve
      }),
    )
    const user = userEvent.setup()
    render(<AdminPage />)

    fillJson('[{"title": "Software Engineer"}]')
    await user.click(screen.getByRole('button', { name: 'Upload jobs' }))

    const button = screen.getByRole('button', { name: 'Uploading…' })
    expect(button).toBeDisabled()

    resolveUpload([mockUploadResult()])

    await waitFor(() => expect(screen.getByRole('button', { name: 'Upload jobs' })).toBeEnabled())
  })

  it('uploads the parsed array and shows a success summary with the result', async () => {
    vi.mocked(uploadJobs).mockResolvedValue([mockUploadResult({ job: mockJob({ title: 'Backend Engineer' }) })])
    const user = userEvent.setup()
    render(<AdminPage />)

    fillJson('[{"title": "Backend Engineer"}]')
    await user.click(screen.getByRole('button', { name: 'Upload jobs' }))

    expect(uploadJobs).toHaveBeenCalledWith([{ title: 'Backend Engineer' }])
    await waitFor(() => {
      expect(screen.getByRole('status')).toHaveTextContent('Uploaded 1 job.')
    })
    const item = screen.getByRole('listitem')
    expect(item).toHaveTextContent('Success')
    expect(item).toHaveTextContent('Backend Engineer')
    expect(screen.queryByText('Not approved')).not.toBeInTheDocument()
  })

  it('summarizes a mix of successes and failures', async () => {
    vi.mocked(uploadJobs).mockResolvedValue([
      mockUploadResult({ job: mockJob({ title: 'Backend Engineer' }) }),
      {
        result: 'error',
        job: { title: 'Bad Job', company: 'Acme', location: 'USA', salary: 1, employment_type: 'Full-Time',
          posting_date: '2026-08-01', company_type: 'Direct Employer' },
        error: 'salaryValue must have no more than 2 decimal places',
      },
    ])
    const user = userEvent.setup()
    render(<AdminPage />)

    fillJson('[{"title": "Backend Engineer"}, {"title": "Bad Job"}]')
    await user.click(screen.getByRole('button', { name: 'Upload jobs' }))

    await waitFor(() => {
      expect(screen.getByRole('status')).toHaveTextContent('Uploaded 1 job. 1 failed.')
    })
    const items = screen.getAllByRole('listitem')
    expect(items).toHaveLength(2)
    expect(items[0]).toHaveTextContent('Success')
    expect(items[0]).toHaveTextContent('Backend Engineer')
    expect(items[1]).toHaveTextContent('Error')
    expect(items[1]).toHaveTextContent('Bad Job')
    expect(items[1]).toHaveTextContent('salaryValue must have no more than 2 decimal places')
  })

  it('flags a successfully saved job that was not approved', async () => {
    vi.mocked(uploadJobs).mockResolvedValue([
      mockUploadResult({
        job: mockJob({ title: 'Underpaid Role', approved: false, rejectionReason: 'Annual salary must be over $100,000 USD' }),
      }),
    ])
    const user = userEvent.setup()
    render(<AdminPage />)

    fillJson('[{"title": "Underpaid Role"}]')
    await user.click(screen.getByRole('button', { name: 'Upload jobs' }))

    const item = await screen.findByRole('listitem')
    expect(item).toHaveTextContent('Not approved')
    expect(item).toHaveTextContent('Annual salary must be over $100,000 USD')
  })

  it('shows a network error message when uploadJobs rejects', async () => {
    vi.mocked(uploadJobs).mockRejectedValue(new Error('network error'))
    const user = userEvent.setup()
    render(<AdminPage />)

    fillJson('[{"title": "Software Engineer"}]')
    await user.click(screen.getByRole('button', { name: 'Upload jobs' }))

    await waitFor(() => {
      expect(screen.getByRole('status')).toHaveTextContent(
        'Could not reach the jobs API. Is the backend running on localhost:8080?',
      )
    })
  })
})
