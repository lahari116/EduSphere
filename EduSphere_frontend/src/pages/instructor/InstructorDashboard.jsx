import { useQuery, useQueries } from '@tanstack/react-query'
import { useAuth } from '../../context/AuthContext'
import { courseService } from '../../services/courseService'
import { enrollmentService } from '../../services/enrollmentService'
import { analyticsService } from '../../services/analyticsService'
import StatCard from '../../components/dashboard/StatCard'
import { PageLoader } from '../../components/common/LoadingSpinner'
import { BookOpen, Users, ClipboardList, TrendingUp, ArrowRight, GraduationCap } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { formatDate, getGreeting, getGreetingEmoji } from '../../utils/helpers'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'

export default function InstructorDashboard() {
  const { user } = useAuth()
  const navigate = useNavigate()

  // Get instructor's own enrolled courses
  const { data: instructorEnrollData, isLoading } = useQuery({
    queryKey: ['instructor-enrollments', user?.userId],
    queryFn: () => enrollmentService.getInstructorEnrollments(user.userId),
    enabled: !!user?.userId,
  })

  const { data: allCoursesData } = useQuery({
    queryKey: ['all-courses'],
    queryFn: () => courseService.getAll(),
  })

  const { data: kpiData } = useQuery({
    queryKey: ['kpis'],
    queryFn: () => analyticsService.getKpis(),
  })

  if (isLoading) return <PageLoader />

  const instructorEnrollments = instructorEnrollData?.data?.data || []
  const allCourses = allCoursesData?.data?.data || []
  const courseMap = Object.fromEntries(allCourses.map((c) => [c.courseId, c]))

  const myCourses = instructorEnrollments
    .map((e) => courseMap[e.courseId])
    .filter(Boolean)

  const kpis = kpiData?.data?.data || {}

  const chartData = myCourses.slice(0, 6).map((c) => ({
    name: (c.courseCode ?? c.courseName ?? '').slice(0, 10),
    code: c.courseCode,
  }))

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Welcome banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-emerald-500 via-teal-500 to-cyan-600 p-6 text-white shadow-glow">
        <div className="absolute right-0 top-0 w-64 h-full opacity-10">
          <div className="absolute right-[-30px] top-[-30px] w-48 h-48 rounded-full bg-white" />
          <div className="absolute right-24 bottom-[-20px] w-32 h-32 rounded-full bg-white" />
        </div>
        <div className="relative z-10 flex items-start justify-between gap-4">
          <div>
            <p className="text-white/70 text-sm font-medium flex items-center gap-1.5">
              {getGreetingEmoji()} {getGreeting()}
            </p>
            <h2 className="text-3xl font-bold mt-0.5">Prof. {user?.firstName} {user?.lastName}</h2>
            <p className="text-white/70 text-sm mt-2">
              You are teaching <strong className="text-white">{myCourses.length}</strong> course{myCourses.length !== 1 ? 's' : ''}.
            </p>
          </div>
          <div className="w-14 h-14 rounded-2xl bg-white/20 backdrop-blur flex items-center justify-center flex-shrink-0">
            <GraduationCap size={28} className="text-white" />
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard label="My Courses"      value={myCourses.length}               icon={BookOpen}     colorIndex={1} />
        <StatCard label="Total Students"  value={kpis.totalStudents ?? '—'}      icon={Users}        colorIndex={0} />
        <StatCard label="Assignments"     value={kpis.totalAssignments ?? '—'}   icon={ClipboardList} colorIndex={2} />
        <StatCard label="Completion Rate" value={`${kpis.completionRate ?? 0}%`} icon={TrendingUp}   colorIndex={3} />
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* My courses list */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="section-title">My Assigned Courses</h3>
            <button
              onClick={() => navigate('/instructor/courses')}
              className="text-sm text-primary-600 hover:underline font-medium flex items-center gap-1"
            >
              View all <ArrowRight size={14} />
            </button>
          </div>
          <div className="space-y-3">
            {myCourses.slice(0, 6).map((c) => (
              <div
                key={c.courseId}
                onClick={() => navigate(`/instructor/courses/${c.courseId}`)}
                className="flex items-center gap-3 p-3 rounded-xl hover:bg-emerald-50 dark:hover:bg-emerald-900/20 cursor-pointer transition-colors group"
              >
                <div className="w-9 h-9 rounded-lg bg-emerald-100 flex items-center justify-center flex-shrink-0">
                  <BookOpen size={16} className="text-emerald-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold truncate" style={{ color: 'var(--text-primary)' }}>{c.courseName}</p>
                  <p className="text-xs" style={{ color: 'var(--text-muted)' }}>
                    {c.courseCode}
                    {c.completionDeadline && ` · Deadline ${formatDate(c.completionDeadline)}`}
                  </p>
                </div>
                <ArrowRight size={15} className="text-slate-300 group-hover:text-emerald-500 transition-colors flex-shrink-0" />
              </div>
            ))}
            {myCourses.length === 0 && (
              <div className="text-center py-10">
                <BookOpen size={36} className="mx-auto mb-2 text-slate-200" />
                <p className="text-sm" style={{ color: 'var(--text-muted)' }}>No courses assigned yet.</p>
                <p className="text-xs mt-1" style={{ color: 'var(--text-muted)' }}>Contact your coordinator to be enrolled in a course.</p>
              </div>
            )}
          </div>
        </div>

        {/* Quick stats */}
        <div className="card">
          <h3 className="section-title mb-5">Platform Overview</h3>
          {myCourses.length === 0 ? (
            <p className="text-sm text-center py-8" style={{ color: 'var(--text-muted)' }}>No courses yet</p>
          ) : (
            <ResponsiveContainer width="100%" height={200}>
              <BarChart
                data={myCourses.slice(0, 6).map((c) => ({ name: (c.courseCode ?? '').slice(0, 10), fullName: c.courseName }))}
                margin={{ top: 0, right: 10, bottom: 0, left: -20 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                <XAxis dataKey="name" tick={{ fontSize: 11, fill: 'var(--text-muted)' }} />
                <YAxis tick={{ fontSize: 11, fill: 'var(--text-muted)' }} />
                <Tooltip
                  contentStyle={{ borderRadius: 10, fontSize: 12 }}
                  formatter={() => ['']}
                  labelFormatter={(label, payload) => payload?.[0]?.payload?.fullName || label}
                />
                <Bar dataKey="name" name="Course" fill="#10b981" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
          <div className="mt-4 grid grid-cols-2 gap-3">
            <div className="bg-emerald-50 dark:bg-emerald-900/20 rounded-xl p-3 text-center">
              <p className="text-2xl font-bold text-emerald-600">{myCourses.length}</p>
              <p className="text-xs mt-0.5" style={{ color: 'var(--text-muted)' }}>My Courses</p>
            </div>
            <div className="bg-blue-50 dark:bg-blue-900/20 rounded-xl p-3 text-center">
              <p className="text-2xl font-bold text-blue-600">{kpis.totalStudents ?? '—'}</p>
              <p className="text-xs mt-0.5" style={{ color: 'var(--text-muted)' }}>Total Students</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
