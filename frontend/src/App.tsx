import { useEffect, useRef, useState, type FormEvent } from 'react'
import { fetchJobs, type Job, type SortBy, type SortDirection } from './api.ts'
import { JobCard } from './components/JobCard.tsx'

function formatStatus(page: number, size: number, totalElements: number, pageCount: number): string {
  if (totalElements === 0) return 'No jobs found.'
  const start = page * size + 1
  const end = page * size + pageCount
  return `Displaying ${start}-${end} of ${totalElements} job${totalElements === 1 ? '' : 's'}.`
}

export default function App() {
  const [title, setTitle] = useState('')
  const [committedTitle, setCommittedTitle] = useState('')
  const [country, setCountry] = useState('')
  const [sortBy, setSortBy] = useState<SortBy | ''>('')
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc')

  const [jobs, setJobs] = useState<Job[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [status, setStatus] = useState('Search for jobs to get started.')
  const [hasSearched, setHasSearched] = useState(false)

  useEffect(() => {
    if (!hasSearched) return

    setStatus('Loading…')

    fetchJobs({
      title: committedTitle || undefined,
      country: country || undefined,
      sortBy: sortBy || undefined,
      sortDirection,
      page,
    })
      .then((data) => {
        setJobs(data.content)
        setTotalPages(data.totalPages)
        setStatus(formatStatus(data.number, data.size, data.totalElements, data.content.length))
      })
      .catch((err) => {
        setStatus('Something went wrong loading jobs. Please try again.')
        setJobs([])
        setTotalPages(0)
        console.error(err)
      })
  }, [hasSearched, committedTitle, country, sortBy, sortDirection, page])

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setCommittedTitle(title.trim())
    setPage(0)
    setHasSearched(true)
  }

  function handleReset() {
    setTitle('')
    setCommittedTitle('')
    setCountry('')
    setSortBy('')
    setSortDirection('asc')
    setPage(0)
    setTotalPages(0)
    setHasSearched(false)
    setJobs([])
    setStatus('Search for jobs to get started.')
  }

  return (
    <>
      <header className="page-header">
        <h1>Job Search</h1>
        <p>
          Search open roles from the jobs board. <a href="/admin">Admin: upload jobs</a>
        </p>
      </header>

      <form className="search-form" onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="title-input">Job title</label>
          <input
            id="title-input"
            type="text"
            placeholder="e.g. Software Engineer"
            autoComplete="off"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
          />
        </div>

        <div className="field">
          <label htmlFor="country-select">Country</label>
          <select
            id="country-select"
            value={country}
            onChange={(event) => {
              setCountry(event.target.value)
              setPage(0)
              setHasSearched(true)
            }}
          >
            <option value="">All countries</option>
              <option key="USA" value="USA">USA</option>
              <option key="Canada" value="Canada">Canada</option>
          </select>
        </div>

        <div className="field">
          <label htmlFor="sort-by-select">Sort by</label>
          <select
            id="sort-by-select"
            value={sortBy}
            onChange={(event) => {
              setSortBy(event.target.value as SortBy | '')
              setPage(0)
              setHasSearched(true)
            }}
          >
            <option value="">None</option>
            <option value="salary">Salary</option>
            <option value="postingDate">Posting date</option>
          </select>
        </div>

        <div className="field">
          <label htmlFor="sort-direction-select">Direction</label>
          <select
            id="sort-direction-select"
            value={sortDirection}
            onChange={(event) => {
              setSortDirection(event.target.value as SortDirection)
              setPage(0)
              setHasSearched(true)
            }}
          >
            <option value="asc">Ascending</option>
            <option value="desc">Descending</option>
          </select>
        </div>

        <div className="field actions">
          <button type="submit">Search</button>
          <button type="button" onClick={handleReset}>
            Reset
          </button>
        </div>
      </form>

      <p className="status" role="status">
        {status}
      </p>

      <ul className="job-list">
        {jobs.map((job) => (
          <JobCard key={job.id} job={job} />
        ))}
      </ul>

      {hasSearched && totalPages > 1 && (
        <div className="pagination">
          <button type="button" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            Previous
          </button>
          <span>
            Page {page + 1} of {totalPages}
          </span>
          <button type="button" disabled={page + 1 >= totalPages} onClick={() => setPage((p) => p + 1)}>
            Next
          </button>
        </div>
      )}
    </>
  )
}
