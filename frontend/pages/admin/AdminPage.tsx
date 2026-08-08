import { useState, type FormEvent } from 'react'
import { uploadJobs, type Job, type JobUploadResult } from '../../src/api.ts'

function savedJobOf(result: JobUploadResult): Job | null {
  return result.result === 'success' && 'id' in result.job ? result.job : null
}

const PLACEHOLDER = `[
  {
    "title": "Software Engineer",
    "description": "Build things.",
    "company": "Acme Corp",
    "location": { "city": "New York", "state": "NY", "country": "USA" },
    "salary": { "value": 150000, "currency": "USD", "unit": "Annual" },
    "employment_type": "Full-Time",
    "posting_date": "2026-08-01",
    "company_type": "Direct Employer",
    "language": "English",
    "remote": false
  }
]`

export default function AdminPage() {
  const [jsonText, setJsonText] = useState('')
  const [status, setStatus] = useState('')
  const [results, setResults] = useState<JobUploadResult[]>([])
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setResults([])

    let parsed: unknown
    try {
      parsed = JSON.parse(jsonText)
    } catch (err) {
      setStatus(`Invalid JSON: ${(err as Error).message}`)
      return
    }

    if (!Array.isArray(parsed)) {
      setStatus('JSON must be an array of job objects.')
      return
    }

    setSubmitting(true)
    setStatus('Uploading…')

    try {
      const uploadResults = await uploadJobs(parsed)
      setResults(uploadResults)
      const successCount = uploadResults.filter((r) => r.result === 'success').length
      const errorCount = uploadResults.length - successCount
      setStatus(`Uploaded ${successCount} job${successCount === 1 ? '' : 's'}.${errorCount > 0 ? ` ${errorCount} failed.` : ''}`)
    } catch (err) {
      setStatus('Could not reach the jobs API. Is the backend running on localhost:8080?')
      console.error(err)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <header className="page-header">
        <h1>Job Upload Admin</h1>
        <p>
          Paste a JSON array of jobs and submit to add them to the jobs board. <a href="/">Back to job search</a>
        </p>
      </header>

      <form className="upload-form" onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="jobs-json">Jobs JSON</label>
          <textarea
            id="jobs-json"
            rows={16}
            placeholder={PLACEHOLDER}
            value={jsonText}
            onChange={(event) => setJsonText(event.target.value)}
            spellCheck={false}
          />
        </div>

        <div className="field actions">
          <button type="submit" disabled={submitting}>
            {submitting ? 'Uploading…' : 'Upload jobs'}
          </button>
        </div>
      </form>

      {status && (
        <p className="status" role="status">
          {status}
        </p>
      )}

      {results.length > 0 && (
        <ul className="upload-results">
          {results.map((result, index) => {
            const savedJob = savedJobOf(result)
            const notApproved = savedJob !== null && !savedJob.approved

            return (
              <li key={index} className={`upload-result upload-result-${result.result}`}>
                <span className="upload-result-badge">{result.result === 'success' ? 'Success' : 'Error'}</span>
                {notApproved && <span className="upload-result-badge upload-result-badge-unapproved">Not approved</span>}
                <span className="upload-result-title">
                  {'title' in result.job ? result.job.title : `Job ${index + 1}`}
                </span>
                {result.error && <span className="upload-result-message">{result.error}</span>}
                {notApproved && savedJob.rejectionReason && (
                  <span className="upload-result-message">{savedJob.rejectionReason}</span>
                )}
              </li>
            )
          })}
        </ul>
      )}
    </>
  )
}
