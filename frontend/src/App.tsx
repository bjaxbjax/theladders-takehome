import { useEffect, useRef, useState, type FormEvent } from 'react'
import { fetchJobs, type Job, type SortBy, type SortDirection } from './api.ts'
import { JobCard } from './JobCard.tsx'

function formatStatus(count: number): string {
  return count === 0 ? 'No jobs found.' : `${count} job${count === 1 ? '' : 's'} found.`
}

export default function App() {
  const [title, setTitle] = useState('')
  const [committedTitle, setCommittedTitle] = useState('')
  const [country, setCountry] = useState('')
  const [sortBy, setSortBy] = useState<SortBy | ''>('')
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc')

  const [countries, setCountries] = useState<string[]>([])
  const [jobs, setJobs] = useState<Job[]>([])
  const [status, setStatus] = useState('Search for jobs to get started.')
  const [hasSearched, setHasSearched] = useState(false)
  const hasLoadedOnce = useRef(false)

  useEffect(() => {
    if (!hasSearched) return

    let cancelled = false
    setStatus('Loading…')

    fetchJobs({
      title: committedTitle || undefined,
      country: country || undefined,
      sortBy: sortBy || undefined,
      sortDirection,
    })
      .then((data) => {
        if (cancelled) return
        hasLoadedOnce.current = true
        setJobs(data)
        setStatus(formatStatus(data.length))
        setCountries((prev) =>
          prev.length > 0
            ? prev
            : Array.from(new Set(data.map((job) => job.location.country))).sort((a, b) => a.localeCompare(b)),
        )
      })
      .catch((err) => {
        if (cancelled) return
        setStatus(
          hasLoadedOnce.current
            ? 'Something went wrong loading jobs. Please try again.'
            : 'Could not reach the jobs API. Is the backend running on localhost:8080?',
        )
        setJobs([])
        console.error(err)
      })

    return () => {
      cancelled = true
    }
  }, [hasSearched, committedTitle, country, sortBy, sortDirection])

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setCommittedTitle(title.trim())
    setHasSearched(true)
  }

  function handleReset() {
    setTitle('')
    setCommittedTitle('')
    setCountry('')
    setSortBy('')
    setSortDirection('asc')
    setHasSearched(false)
    setJobs([])
    setStatus('Search for jobs to get started.')
  }

  return (
    <>
      <header className="page-header">
        <h1>Job Search</h1>
        <p>
          Search open roles from the jobs board. <a href="/pages/admin/admin.html">Admin: upload jobs</a>
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
              setHasSearched(true)
            }}
          >
            <option value="">All countries</option>
            {countries.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </div>

        <div className="field">
          <label htmlFor="sort-by-select">Sort by</label>
          <select
            id="sort-by-select"
            value={sortBy}
            onChange={(event) => {
              setSortBy(event.target.value as SortBy | '')
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
    </>
  )
}
