import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { assignmentService } from '../../services/assignmentService'
import { PageLoader } from '../../components/common/LoadingSpinner'
import Badge from '../../components/common/Badge'
import { ArrowLeft, UserCheck, UserX } from 'lucide-react'
import { formatDateTime, scoreBg } from '../../utils/helpers'
import clsx from 'clsx'

export default function Submissions() {
  const { assignmentId } = useParams()
  const navigate = useNavigate()

  const { data: submissionsData, isLoading } = useQuery({
    queryKey: ['submissions', assignmentId],
    queryFn: () => assignmentService.getSubmissions(assignmentId),
  })

  const { data: statusData } = useQuery({
    queryKey: ['attempt-status', assignmentId],
    queryFn: () => assignmentService.getAttemptStatus(assignmentId),
  })

  if (isLoading) return <PageLoader />

  const submissions = submissionsData?.data?.data || []
  const status = statusData?.data?.data || {}
  const submitted = status.submitted || []
  const notSubmitted = status.notSubmitted || []
  const avgScore = submissions.reduce((acc, s) => acc + (s.score || 0), 0) / (submissions.length || 1)

  return (
    <div className="space-y-6 animate-fade-in max-w-4xl">
      <button onClick={() => navigate(-1)} className="btn-ghost -ml-2">
        <ArrowLeft size={16} /> Back
      </button>

      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title">Submissions</h1>
          <p className="text-slate-500 text-sm mt-0.5">{submissions.length} submissions received</p>
        </div>
        <div className="flex gap-3">
          <div className="card py-3 px-5 text-center">
            <p className="text-2xl font-bold text-slate-900">{Math.round(avgScore)}%</p>
            <p className="text-xs text-slate-400 mt-0.5">Class Average</p>
          </div>
          <div className="card py-3 px-5 text-center">
            <p className="text-2xl font-bold text-emerald-600">{submitted.length}</p>
            <p className="text-xs text-slate-400 mt-0.5">Submitted</p>
          </div>
          <div className="card py-3 px-5 text-center">
            <p className="text-2xl font-bold text-rose-500">{notSubmitted.length}</p>
            <p className="text-xs text-slate-400 mt-0.5">Not Submitted</p>
          </div>
        </div>
      </div>

      {/* Submissions table */}
      <div className="card overflow-hidden">
        <h3 className="section-title mb-4">Submission Results</h3>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50">
                <th className="text-left py-3 px-4 text-slate-500 font-medium">Student ID</th>
                <th className="text-left py-3 px-4 text-slate-500 font-medium">Score</th>
                <th className="text-left py-3 px-4 text-slate-500 font-medium">Correct</th>
                <th className="text-left py-3 px-4 text-slate-500 font-medium">Submitted</th>
                <th className="text-left py-3 px-4 text-slate-500 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {submissions.map((s) => (
                <tr key={s.submissionId} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                  <td className="py-3 px-4 font-mono text-xs text-slate-600">{s.studentId?.slice(0, 12)}…</td>
                  <td className="py-3 px-4">
                    <span className={clsx('badge', scoreBg(s.score))}>{Math.round(s.score ?? 0)}%</span>
                  </td>
                  <td className="py-3 px-4 text-slate-600">{s.correctAnswers}/{s.totalQuestions}</td>
                  <td className="py-3 px-4 text-slate-500">{formatDateTime(s.submittedAt)}</td>
                  <td className="py-3 px-4">
                    <Badge variant={s.status === 'SUBMITTED' ? 'green' : s.status === 'AUTO_TIMED_OUT' ? 'rose' : 'amber'}>
                      {s.status?.replace('_', ' ')}
                    </Badge>
                  </td>
                </tr>
              ))}
              {submissions.length === 0 && (
                <tr><td colSpan={5} className="py-10 text-center text-slate-400">No submissions yet.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Not submitted */}
      {notSubmitted.length > 0 && (
        <div className="card">
          <h3 className="section-title mb-3 flex items-center gap-2">
            <UserX size={18} className="text-rose-400" /> Not Submitted ({notSubmitted.length})
          </h3>
          <div className="flex flex-wrap gap-2">
            {notSubmitted.map((id) => (
              <span key={id} className="px-3 py-1 bg-rose-50 text-rose-600 rounded-full text-xs font-mono">
                {id?.slice(0, 12)}…
              </span>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
