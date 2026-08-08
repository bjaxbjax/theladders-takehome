import type { Job } from '../api.ts'

function formatLocation(location: Job['location']): string {
  return [location.city, location.state, location.country].filter(Boolean).join(', ')
}

function formatSalary(job: Job): string {
  const amount = new Intl.NumberFormat('en-US').format(job.salaryValue)
  return `${job.salaryCurrency} ${amount} / ${job.salaryPeriod}`
}

function formatDate(dateStr: string): string {
  return new Intl.DateTimeFormat('en-US', { year: 'numeric', month: 'short', day: 'numeric' }).format(
    new Date(dateStr),
  )
}

export function JobCard({ job }: { job: Job }) {
  return (
    <li className="job-card">
      <div className="job-card-header">
        <h2>{job.title}</h2>
        <span className="salary">{formatSalary(job)}</span>
      </div>
      <p className="company">
        {job.company}
        {job.remote && <span className="badge">Remote</span>}
      </p>
      <p className="meta">
        <span>{formatLocation(job.location)}</span>
        <span>{job.employmentType}</span>
        <span>Posted {formatDate(job.postingDate)}</span>
      </p>
    </li>
  )
}
