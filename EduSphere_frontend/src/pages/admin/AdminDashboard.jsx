import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../../context/AuthContext'
import { analyticsService } from '../../services/analyticsService'
import { courseService } from '../../services/courseService'
import { adminService } from '../../services/adminService'
import { enrollmentService } from '../../services/enrollmentService'
import StatCard from '../../components/dashboard/StatCard'
import { PageLoader } from '../../components/common/LoadingSpinner'
import Badge from '../../components/common/Badge'
import { Users, BookOpen, TrendingUp, ClipboardList, GraduationCap, UserCheck, Shield, ListChecks } from 'lucide-react'
import { formatDate, getGreeting, getGreetingEmoji } from '../../utils/helpers'

const ROLE_BADGE = { STUDENT: 'blue', INSTRUCTOR: 'green', COORDINATOR: 'amber', ADMIN: 'rose' }
const ROLE_ICON = { STUDENT: GraduationCap, INSTRUCTOR: BookOpen, COORDINATOR: UserCheck, ADMIN: Shield }
const ROLE_BG   = { STUDENT: 'bg-blue-50 dark:bg-blue-900/20', INSTRUCTOR: 'bg-emerald-50 dark:bg-emerald-900/20', COORDINATOR: 'bg-amber-50 dark:bg-amber-900/20', ADMIN: 'bg-rose-50 dark:bg-rose-900/20' }
const ROLE_TEXT = { STUDENT: 'text-blue-600', INSTRUCTOR: 'text-emerald-600', COORDINATOR: 'text-amber-600', ADMIN: 'text-rose-600' }

export default function AdminDashboard() {
  const { user } = useAuth()

  const { data: kpiData, isLoading: loadingKpi } = useQuery({
    queryKey: ['kpis'],
    queryFn: () => analyticsService.getKpis(),
  })

  const { data: coursesData, isLoading: loadingCourses } = useQuery({
    queryKey: ['courses'],
    queryFn: () => courseService.getAll(),
  })

  const { data: usersData, isLoading: loadingUsers } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => adminService.getUsers(),
  })

  const { data: enrollCountData } = useQuery({
    queryKey: ['admin-enrollment-count'],
    queryFn: () => adminService.getTotalEnrollmentCount(),
    retry: false,
  })

  if (loadingKpi || loadingCourses || loadingUsers) return <PageLoader />

  const kpis    = kpiData?.data?.data    || {}
  const courses = coursesData?.data?.data || []
  const users   = usersData?.data?.data   || []

  const totalUsers        = users.length
  const totalCourses      = courses.length
  const totalStudents     = users.filter(u => u.role === 'STUDENT').length
  const totalInstructors  = users.filter(u => u.role === 'INSTRUCTOR').length
  const totalCoordinators = users.filter(u => u.role === 'COORDINATOR').length
  const totalAdmins       = users.filter(u => u.role === 'ADMIN').length
  const totalEnrollments  = enrollCountData?.data?.data ?? '—'

  const roleRows = [
    { role: 'STUDENT',     count: totalStudents,     label: 'Students' },
    { role: 'INSTRUCTOR',  count: totalInstructors,  label: 'Instructors' },
    { role: 'COORDINATOR', count: totalCoordinators, label: 'Coordinators' },
    { role: 'ADMIN',       count: totalAdmins,       label: 'Admins' },
  ]

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Personalized welcome banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-rose-500 via-pink-500 to-purple-600 p-6 text-white shadow-glow">
        <div className="absolute right-0 top-0 w-64 h-full opacity-10">
          <div className="absolute right-[-30px] top-[-30px] w-48 h-48 rounded-full bg-white" />
          <div className="absolute right-24 bottom-[-20px] w-32 h-32 rounded-full bg-white" />
        </div>
        <div className="relative z-10 flex items-start justify-between gap-4">
          <div>
            <p className="text-white/70 text-sm font-medium flex items-center gap-1.5">
              {getGreetingEmoji()} {getGreeting()}
            </p>
            <h2 className="text-3xl font-bold mt-0.5">
              {user?.firstName} {user?.lastName}
            </h2>
            <p className="text-white/70 text-sm mt-2">Full platform control at your fingertips.</p>
          </div>
          <div className="w-14 h-14 rounded-2xl bg-white/20 backdrop-blur flex items-center justify-center flex-shrink-0">
            <Shield size={26} className="text-white" />
          </div>
        </div>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
        <StatCard label="Total Users"        value={totalUsers}          icon={Users}         colorIndex={0} />
        <StatCard label="Total Courses"      value={totalCourses}        icon={BookOpen}      colorIndex={1} />
        <StatCard label="Coordinators"       value={totalCoordinators}   icon={UserCheck}     colorIndex={2} />
        <StatCard label="Total Students"     value={totalStudents}       icon={GraduationCap} colorIndex={3} />
        <StatCard label="Instructors"        value={totalInstructors}    icon={ClipboardList} colorIndex={4} />
        <StatCard label="Total Enrollments"  value={totalEnrollments}    icon={ListChecks}    colorIndex={5} />
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* User breakdown by role */}
        <div className="card">
          <h3 className="section-title mb-4">Users by Role</h3>
          <div className="space-y-3">
            {roleRows.map(({ role, count, label }) => {
              const Icon = ROLE_ICON[role]
              const pct  = totalUsers > 0 ? Math.round((count / totalUsers) * 100) : 0
              return (
                <div key={role} className="flex items-center gap-4">
                  <div className={`w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0 ${ROLE_BG[role]}`}>
                    <Icon size={16} className={ROLE_TEXT[role]} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-sm font-medium" style={{ color: 'var(--text-secondary)' }}>{label}</span>
                      <span className="text-sm font-bold" style={{ color: 'var(--text-primary)' }}>{count}</span>
                    </div>
                    <div className="h-2 rounded-full overflow-hidden" style={{ backgroundColor: 'var(--border)' }}>
                      <div
                        className={`h-full rounded-full transition-all duration-500 ${
                          role === 'STUDENT' ? 'bg-blue-400' :
                          role === 'INSTRUCTOR' ? 'bg-emerald-400' :
                          role === 'COORDINATOR' ? 'bg-amber-400' : 'bg-rose-400'
                        }`}
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  </div>
                  <span className="text-xs w-10 text-right flex-shrink-0" style={{ color: 'var(--text-muted)' }}>{pct}%</span>
                </div>
              )
            })}
            {totalUsers === 0 && (
              <p className="text-sm text-center py-4" style={{ color: 'var(--text-muted)' }}>No users found.</p>
            )}
          </div>
        </div>

        {/* Course overview */}
        <div className="card">
          <h3 className="section-title mb-4">Course Overview</h3>
          <div className="space-y-3">
            <div className="flex items-center justify-between p-4 bg-primary-50 dark:bg-primary-900/20 rounded-xl">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-primary-500 flex-shrink-0" />
                <span className="text-sm font-medium" style={{ color: 'var(--text-secondary)' }}>Total Courses</span>
              </div>
              <span className="text-xl font-bold text-primary-600">{totalCourses}</span>
            </div>
            <div className="flex items-center justify-between p-4 bg-emerald-50 dark:bg-emerald-900/20 rounded-xl">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-emerald-500 flex-shrink-0" />
                <span className="text-sm font-medium" style={{ color: 'var(--text-secondary)' }}>Total Students</span>
              </div>
              <span className="text-xl font-bold text-emerald-600">{totalStudents}</span>
            </div>
            <div className="flex items-center justify-between p-4 bg-amber-50 dark:bg-amber-900/20 rounded-xl">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-amber-400 flex-shrink-0" />
                <span className="text-sm font-medium" style={{ color: 'var(--text-secondary)' }}>Completion Rate</span>
              </div>
              <span className="text-xl font-bold text-amber-600">{kpis.completionRate ?? 0}%</span>
            </div>
          </div>
        </div>
      </div>

      {/* Recent users */}
      <div className="card overflow-hidden">
        <div className="flex items-center justify-between mb-4">
          <h3 className="section-title">Recent Users</h3>
          <a href="/admin/users" className="text-sm text-primary-600 dark:text-primary-400 font-medium hover:underline">View all →</a>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b" style={{ borderColor: 'var(--border)', backgroundColor: 'var(--bg-base)' }}>
                <th className="text-left py-3 px-4 font-medium" style={{ color: 'var(--text-muted)' }}>Name</th>
                <th className="text-left py-3 px-4 font-medium" style={{ color: 'var(--text-muted)' }}>Email</th>
                <th className="text-left py-3 px-4 font-medium" style={{ color: 'var(--text-muted)' }}>Role</th>
              </tr>
            </thead>
            <tbody>
              {users.slice(0, 5).map((u) => (
                <tr key={u.userId} className="border-b transition-colors hover:bg-primary-50/50 dark:hover:bg-primary-900/10" style={{ borderColor: 'var(--border)' }}>
                  <td className="py-3 px-4 font-medium" style={{ color: 'var(--text-primary)' }}>{u.firstName} {u.lastName}</td>
                  <td className="py-3 px-4" style={{ color: 'var(--text-muted)' }}>{u.email}</td>
                  <td className="py-3 px-4">
                    <Badge variant={ROLE_BADGE[u.role] || 'slate'}>{u.role}</Badge>
                  </td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr><td colSpan={4} className="py-8 text-center" style={{ color: 'var(--text-muted)' }}>No users found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
