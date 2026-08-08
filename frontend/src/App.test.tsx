import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import App from './App.tsx'
import { fetchJobs } from './api.ts'
import { mockJob, mockPage } from './test/fixtures.ts'

vi.mock('./api.ts', () => ({
  fetchJobs: vi.fn(),
}))

describe('App', () => {
  beforeEach(() => {
    vi.mocked(fetchJobs).mockReset()
  })

  it('shows a starting message and does not fetch jobs before a search is submitted', () => {
    render(<App />)

    expect(screen.getByRole('status')).toHaveTextContent('Search for jobs to get started.')
    expect(fetchJobs).not.toHaveBeenCalled()
  })

  it('fetches and displays jobs after submitting a search, with a range in the status message', async () => {
    const jobs = Array.from({ length: 10 }, (_, i) => mockJob({ id: 11 + i }))
    vi.mocked(fetchJobs).mockResolvedValue(
      mockPage({
        content: jobs,
        totalElements: 54,
        totalPages: 6,
        number: 1,
        size: 10,
      }),
    )
    const user = userEvent.setup()
    render(<App />)

    await user.click(screen.getByRole('button', { name: 'Search' }))

    await waitFor(() => {
      expect(screen.getByRole('status')).toHaveTextContent('Displaying 11-20 of 54 jobs.')
    })
    expect(screen.getAllByRole('listitem')).toHaveLength(10)
  })

  it('shows "No jobs found." when the search returns no results', async () => {
    vi.mocked(fetchJobs).mockResolvedValue(mockPage({ content: [], totalElements: 0, totalPages: 0 }))
    const user = userEvent.setup()
    render(<App />)

    await user.click(screen.getByRole('button', { name: 'Search' }))

    await waitFor(() => {
      expect(screen.getByRole('status')).toHaveTextContent('No jobs found.')
    })
  })

  it('shows an error message when the fetch fails', async () => {
    vi.mocked(fetchJobs).mockRejectedValue(new Error('network error'))
    const user = userEvent.setup()
    render(<App />)

    await user.click(screen.getByRole('button', { name: 'Search' }))

    await waitFor(() => {
      expect(screen.getByRole('status')).toHaveTextContent('Something went wrong loading jobs. Please try again.')
    })
  })

  it('hides pagination controls when there is only one page', async () => {
    vi.mocked(fetchJobs).mockResolvedValue(mockPage({ totalPages: 1 }))
    const user = userEvent.setup()
    render(<App />)

    await user.click(screen.getByRole('button', { name: 'Search' }))

    await waitFor(() => expect(fetchJobs).toHaveBeenCalledTimes(1))
    expect(screen.queryByText(/Page \d+ of \d+/)).not.toBeInTheDocument()
  })

  it('navigates to the next page and requests it with the updated page number', async () => {
    vi.mocked(fetchJobs).mockResolvedValue(mockPage({ totalPages: 3, number: 0 }))
    const user = userEvent.setup()
    render(<App />)

    await user.click(screen.getByRole('button', { name: 'Search' }))
    await waitFor(() => expect(screen.getByText('Page 1 of 3')).toBeInTheDocument())
    expect(screen.getByRole('button', { name: 'Previous' })).toBeDisabled()

    vi.mocked(fetchJobs).mockResolvedValue(mockPage({ totalPages: 3, number: 1 }))
    await user.click(screen.getByRole('button', { name: 'Next' }))

    await waitFor(() => expect(screen.getByText('Page 2 of 3')).toBeInTheDocument())
    expect(fetchJobs).toHaveBeenLastCalledWith(expect.objectContaining({ page: 1 }))
    expect(screen.getByRole('button', { name: 'Previous' })).not.toBeDisabled()
  })

  it('disables the Next button on the last page', async () => {
    vi.mocked(fetchJobs).mockResolvedValue(mockPage({ totalPages: 3, number: 0 }))
    const user = userEvent.setup()
    render(<App />)

    await user.click(screen.getByRole('button', { name: 'Search' }))
    await waitFor(() => expect(screen.getByText('Page 1 of 3')).toBeInTheDocument())

    vi.mocked(fetchJobs).mockResolvedValue(mockPage({ totalPages: 3, number: 1 }))
    await user.click(screen.getByRole('button', { name: 'Next' }))
    await waitFor(() => expect(screen.getByText('Page 2 of 3')).toBeInTheDocument())

    vi.mocked(fetchJobs).mockResolvedValue(mockPage({ totalPages: 3, number: 2 }))
    await user.click(screen.getByRole('button', { name: 'Next' }))
    await waitFor(() => expect(screen.getByText('Page 3 of 3')).toBeInTheDocument())

    expect(screen.getByRole('button', { name: 'Next' })).toBeDisabled()
  })

  it('resets to page 0 when the country filter changes after paging forward', async () => {
    vi.mocked(fetchJobs).mockResolvedValue(mockPage({ totalPages: 3, number: 0 }))
    const user = userEvent.setup()
    render(<App />)

    await user.click(screen.getByRole('button', { name: 'Search' }))
    await waitFor(() => expect(screen.getByText('Page 1 of 3')).toBeInTheDocument())

    vi.mocked(fetchJobs).mockResolvedValue(mockPage({ totalPages: 3, number: 1 }))
    await user.click(screen.getByRole('button', { name: 'Next' }))
    await waitFor(() => expect(screen.getByText('Page 2 of 3')).toBeInTheDocument())

    vi.mocked(fetchJobs).mockResolvedValue(mockPage({ totalPages: 2, number: 0 }))
    await user.selectOptions(screen.getByLabelText('Country'), 'Canada')

    await waitFor(() => {
      expect(fetchJobs).toHaveBeenLastCalledWith(expect.objectContaining({ country: 'Canada', page: 0 }))
    })
  })

  it('clears results and status on reset', async () => {
    vi.mocked(fetchJobs).mockResolvedValue(mockPage())
    const user = userEvent.setup()
    render(<App />)

    await user.click(screen.getByRole('button', { name: 'Search' }))
    await waitFor(() => expect(screen.getAllByRole('listitem')).toHaveLength(1))

    await user.click(screen.getByRole('button', { name: 'Reset' }))

    expect(screen.getByRole('status')).toHaveTextContent('Search for jobs to get started.')
    expect(screen.queryAllByRole('listitem')).toHaveLength(0)
  })
})
