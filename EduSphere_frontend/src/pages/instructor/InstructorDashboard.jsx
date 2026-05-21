import { useQuery, useQueries } from '@tanstack/react-query'
import { useAuth } from '../../context/AuthContext'
import { courseService } from '../../services/courseService'
import { enrollmentService } from '../../services/enrollmentService'
import { analyticsService } from '../../services/analyticsService'
import StatCard from '../../components/dashboard/StatCard'
import { PageLoader } from '../../components/common/LoadingSpinner'
import { BookOpen, Users, ClipboardList, TrendingUp, ArrowRight } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { formatDate } from '../../utils/helpers'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'

export default function InstructorDashboard() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const { data: coursesData, isLoading } = useQuery({
    queryKey: ['all-courses'],
    queryFn: () => courseService.getAll(),
  })

  const { data: kpiData } = useQuery({
    queryKey: ['kpis'],
    queryFn: () => analyticsService.getKpis(),
  })

  const courses = coursesData?.data?.data || []
  const chartCourses = courses.slice(0, 6)

  const enrollmentQueries = useQueries({
    queries: chartCourses.map((c) => ({
      queryKey: ['enrollment-count', c.courseId],
      queryFn: () => enrollmentService.getEnrollmentsByCourse(c.courseId),
      enabled: !!c.courseId,
      staleTime: 5 * 60 * 1000,
    })),
  })

  if (isLoading) return <PageLoader />

  const kpis = kpiData?.data?.data || {}

  const chartData = chartCourses.map((c, i) => {
    const enrollments = enrollmentQueries[i]?.data?.data?.data || []
    const studentCount = enrollments.filter((e) => e.userRole === 'STUDENT' && e.status === 'ACTIVE').length
    return {
      name: (c.courseCode ?? c.courseName ?? '').slice(0, 10),
      enrolled: studentCount,
    }
  })

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Welcome banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-emerald-500 via-teal-500 to-cyan-600 p-6 text-white">
        <div className="absolute right-0 top-0 w-64 h-full opacity-10">
          <div className="absolute right-[-30px] top-[-30px] w-48 h-48 rounded-full bg-white" />
          <div className="absolute right-24 bottom-[-20px] w-32 h-32 rounded-full bg-white" />
        </div>
        <div className="relative z-10">
          <p className="text-white/70 text-sm font-medium">Welcome back,</p>
          <h2 className="text-3xl font-bold mt-0.5">Prof. {user?.firstName} {user?.lastName} 🎓</h2>
          <p className="text-white/70 text-sm mt-2">
            Manage your enrolled courses, assignments, and track student progress.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard label="Platform Courses"  value={courses.length}              icon={BookOpen}     colorIndex={1} />
        <StatCard label="Total Students"    value={kpis.totalStudents ?? '—'}   icon={Users}        colorIndex={0} />
        <StatCard label="Assignments"       value={kpis.totalAssignments ?? '—'} icon={ClipboardList} colorIndex={2} />
        <StatCard label="Completion Rate"   value={`${kpis.completionRate ?? 0}%`} icon={TrendingUp} colorIndex={3} />
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* Courses list — click to manage */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="section-title">Courses (click to manage)</h3>
          </div>
          <div className="space-y-3">
            {courses.slice(0, 6).map((c) => (
              <div
                key={c.courseId}
                onClick={() => navigate(`/instructor/courses/${c.courseId}`)}
                className="flex items-center gap-3 p-3 rounded-xl hover:bg-emerald-50 cursor-pointer transition-colors group"
              >
                <div className="w-9 h-9 rounded-lg bg-emerald-100 flex items-center justify-center flex-shrink-0">
                  <BookOpen size={16} className="text-emerald-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-slate-800 truncate">{c.courseName}</p>
                  <p className="text-xs text-slate-400">{c.courseCode} · Due {formatDate(c.completionDeadline)}</p>
                </div>
                <ArrowRight size={15} className="text-slate-300 group-hover:text-emerald-500 transition-colors flex-shrink-0" />
              </div>
            ))}
            {courses.length === 0 && (
              <p className="text-sm text-slate-400 text-center py-6">
                Enroll in a course to start managing it.
              </p>
            )}
          </div>
        </div>

        {/* Chart placeholder */}
        <div className="card">
          <h3 className="section-title mb-5">Course Overview</h3>
          {chartData.length === 0 ? (
            <p className="text-slate-400 text-sm text-center py-8">No courses yet</p>
          ) : (
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={chartData} margin={{ top: 0, right: 10, bottom: 0, left: -20 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#94a3b8' }} />
                <YAxis tick={{ fontSize: 11, fill: '#94a3b8' }} />
                <Tooltip contentStyle={{ borderRadius: 10, fontSize: 12 }} />
                <Bar dataKey="enrolled" name="Students" fill="#10b981" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
          <p className="text-xs text-slate-400 text-center mt-2">
            Go to a course to see its enrollment details
          </p>
        </div>
      </div>
    </div>
  )
}
