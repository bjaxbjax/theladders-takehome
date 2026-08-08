import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { JobCard } from './JobCard.tsx'
import { mockJob } from '../test/fixtures.ts'

describe('JobCard', () => {
  it('renders title, company, salary, location, employment type, and posting date', () => {
    render(<JobCard job={mockJob()} />)

    const expectedDate = new Intl.DateTimeFormat('en-US', { year: 'numeric', month: 'short', day: 'numeric' }).format(
      new Date('2026-08-01'),
    )

    expect(screen.getByText('Software Engineer')).toBeInTheDocument()
    expect(screen.getByText('Acme Corp')).toBeInTheDocument()
    expect(screen.getByText('USD 150,000 / Annual')).toBeInTheDocument()
    expect(screen.getByText('New York, NY, USA')).toBeInTheDocument()
    expect(screen.getByText('Full-Time')).toBeInTheDocument()
    expect(screen.getByText(`Posted ${expectedDate}`)).toBeInTheDocument()
  })

  it('shows a Remote badge when the job is remote', () => {
    render(<JobCard job={mockJob({ remote: true })} />)

    expect(screen.getByText('Remote')).toBeInTheDocument()
  })

  it('does not show a Remote badge when the job is not remote', () => {
    render(<JobCard job={mockJob({ remote: false })} />)

    expect(screen.queryByText('Remote')).not.toBeInTheDocument()
  })

  it('omits missing city/state from the location line', () => {
    render(<JobCard job={mockJob({ location: { id: 2, city: null, state: null, country: 'Canada' } })} />)

    expect(screen.getByText('Canada')).toBeInTheDocument()
  })
})
