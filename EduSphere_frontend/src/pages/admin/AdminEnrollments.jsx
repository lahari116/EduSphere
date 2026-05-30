import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { enrollmentService } from '../../services/enrollmentService'
import { PageLoader } from '../../components/common/LoadingSpinner'
import Badge from '../../components/common/Badge'
import { History, Users, Search, BookOpen, Clock } from 'lucide-react'
import { formatDate } from '../../utils/helpers'

const STATUS_BADGE = {
  ACTIVE:    'green',
  PENDING:   'amber',
  REJECTED:  'red',
  COMPLETED: 'blue',
  DROPPED:   'slate',
}

export default function AdminEnrollments() {
  const [search, setSearch] = useState('')

  const { data: pastData, isLoading } = useQuery({
    queryKey: ['admin-enrollments-past'],
    queryFn: () => enrollmentService.getPastEnrollments(),
  })

  if (isLoading) return <PageLoader />

  const past = pastData?.data?.data || []

  const filtered = past.filter((e) => {
    if (!search) return true
    const q = search.toLowerCase()
    return (
      e.userId?.toLowerCase().includes(q) ||
      e.courseId?.toLowerCase().includes(q) ||
      e.status?.toLowerCase().includes(q) ||
      e.userRole?.toLowerCase().includes(q)
    )
  })

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-slate-600 via-slate-700 to-slate-800 p-6 text-white shadow-glow">
        <div className="absolute right-0 top-0 w-64 h-full opacity-10">
          <div className="absolute right-[-30px] top-[-30px] w-48 h-48 rounded-full bg-white" />
          <div className="absolute right-24 bottom-[-20px] w-32 h-32 rounded-full bg-white" />
        </div>
        <div className="relative z-10 flex items-start justify-between gap-4">
          <div>
            <p className="text-white/70 text-sm font-medium">Administration</p>
            <h2 className="text-3xl font-bold mt-0.5">Enrollment History</h2>
            <p className="text-white/70 text-sm mt-2">
              View all past (deleted) enrollment records
            </p>
          </div>
          <div className="w-14 h-14 rounded-2xl bg-white/20 backdrop-blur flex items-center justify-center flex-shrink-0">
            <History size={26} className="text-white" />
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="card text-center">
          <div className="w-12 h-12 rounded-2xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center mx-auto mb-3">
            <History size={22} className="text-slate-600 dark:text-slate-400" />
          </div>
          <p className="text-3xl font-bold" style={{ color: 'var(--text-primary)' }}>{past.length}</p>
          <p className="text-sm mt-1" style={{ color: 'var(--text-muted)' }}>Past Enrollments</p>
        </div>
        <div className="card text-center">
          <div className="w-12 h-12 rounded-2xl bg-rose-100 dark:bg-rose-900/30 flex items-center justify-center mx-auto mb-3">
            <Users size={22} className="text-rose-600 dark:text-rose-400" />
          </div>
          <p className="text-3xl font-bold" style={{ color: 'var(--text-primary)' }}>
            {new Set(past.map((e) => e.userId)).size}
          </p>
          <p className="text-sm mt-1" style={{ color: 'var(--text-muted)' }}>Unique Users</p>
        </div>
        <div className="card text-center">
          <div className="w-12 h-12 rounded-2xl bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center mx-auto mb-3">
            <BookOpen size={22} className="text-blue-600 dark:text-blue-400" />
          </div>
          <p className="text-3xl font-bold" style={{ color: 'var(--text-primary)' }}>
            {new Set(past.map((e) => e.courseId)).size}
          </p>
          <p className="text-sm mt-1" style={{ color: 'var(--text-muted)' }}>Courses Affected</p>
        </div>
      </div>

      {/* Table */}
      <div className="card overflow-hidden">
        <div className="flex items-center justify-between mb-4 flex-wrap gap-3">
          <h3 className="section-title">Past Enrollments</h3>
          <div className="relative">
            <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2" style={{ color: 'var(--text-muted)' }} />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by user, course, role…"
              className="input pl-9 py-1.5 text-sm w-64"
            />
          </div>
        </div>

        {filtered.length === 0 ? (
          <div className="text-center py-16" style={{ color: 'var(--text-muted)' }}>
            <History size={48} className="mx-auto mb-4 opacity-20" />
            <p className="text-base font-semibold" style={{ color: 'var(--text-primary)' }}>No past enrollments found</p>
            <p className="text-sm mt-1">Deleted enrollments will appear here.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b" style={{ borderColor: 'var(--border)', backgroundColor: 'var(--bg-base)' }}>
                  <th className="text-left py-3 px-4 font-medium" style={{ color: 'var(--text-muted)' }}>#</th>
                  <th className="text-left py-3 px-4 font-medium" style={{ color: 'var(--text-muted)' }}>Enrollment ID</th>
                  <th className="text-left py-3 px-4 font-medium" style={{ color: 'var(--text-muted)' }}>User ID</th>
                  <th className="text-left py-3 px-4 font-medium" style={{ color: 'var(--text-muted)' }}>Course ID</th>
                  <th className="text-left py-3 px-4 font-medium" style={{ color: 'var(--text-muted)' }}>Role</th>
                  <th className="text-left py-3 px-4 font-medium" style={{ color: 'var(--text-muted)' }}>Status</th>
                  <th className="text-left py-3 px-4 font-medium" style={{ color: 'var(--text-muted)' }}>Enrolled</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((e, idx) => (
                  <tr
                    key={e.enrollmentId}
                    className="border-b transition-colors hover:bg-slate-50 dark:hover:bg-slate-800/50"
                    style={{ borderColor: 'var(--border)' }}
                  >
                    <td className="py-3 px-4 text-xs" style={{ color: 'var(--text-muted)' }}>{idx + 1}</td>
                    <td className="py-3 px-4">
                      <span className="font-mono text-xs px-2 py-0.5 rounded" style={{ backgroundColor: 'var(--border)', color: 'var(--text-muted)' }}>
                        {e.enrollmentId?.slice(0, 8)}…
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <span className="font-mono text-xs" style={{ color: 'var(--text-secondary)' }}>
                        {e.userId?.slice(0, 8)}…
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <span className="font-mono text-xs" style={{ color: 'var(--text-secondary)' }}>
                        {e.courseId?.slice(0, 8)}…
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <Badge variant={e.userRole === 'INSTRUCTOR' ? 'green' : 'blue'}>
                        {e.userRole || '—'}
                      </Badge>
                    </td>
                    <td className="py-3 px-4">
                      <Badge variant={STATUS_BADGE[e.status] || 'slate'}>
                        {e.status || '—'}
                      </Badge>
                    </td>
                    <td className="py-3 px-4 text-xs flex items-center gap-1" style={{ color: 'var(--text-muted)' }}>
                      <Clock size={11} />
                      {e.enrolledAt ? formatDate(e.enrolledAt) : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
