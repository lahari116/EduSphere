import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../../context/AuthContext'
import { enrollmentService } from '../../services/enrollmentService'
import { assignmentService } from '../../services/assignmentService'
import { analyticsService } from '../../services/analyticsService'
import StatCard from '../../components/dashboard/StatCard'
import CourseCard from '../../components/dashboard/CourseCard'
import { PageLoader } from '../../components/common/LoadingSpinner'
import ProgressBar from '../../components/common/ProgressBar'
import { BookOpen, ClipboardList, Trophy, TrendingUp } from 'lucide-react'
import { scoreBg } from '../../utils/helpers'
import { RadialBarChart, RadialBar, ResponsiveContainer, Tooltip } from 'recharts'
import clsx from 'clsx'
import { Link } from 'react-router-dom'

export default function StudentDashboard() {
  const { user } = useAuth()

  const { data: enrollData, isLoading: loadEnroll } = useQuery({
    queryKey: ['student-enrollments', user?.userId],
    queryFn: () => enrollmentService.getStudentEnrollments(user.userId),
    enabled: !!user?.userId,
  })

  const { data: analyticsData } = useQuery({
    queryKey: ['student-analytics-progress', user?.userId],
    queryFn: () => analyticsService.getStudentProgress(user.userId),
    enabled: !!user?.userId,
  })

  const { data: submissionsData } = useQuery({
    queryKey: ['student-assignment-progress', user?.userId],
    queryFn: () => assignmentService.getStudentProgress(user.userId),
    enabled: !!user?.userId,
  })

  if (loadEnroll) return <PageLoader />

  const enrollments = enrollData?.data?.data || []
  const analytics = analyticsData?.data?.data || []
  const subStats = submissionsData?.data?.data || {}

  const avgScore = subStats.averageScore ?? 0
  const totalAssignments = subStats.totalAssignments ?? 0
  const submittedAssignments = subStats.submittedAssignments ?? 0
  const pending = Math.max(0, totalAssignments - submittedAssignments)

  const radialData = [{ name: 'Score', value: Math.round(avgScore), fill: '#8b5cf6' }]

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Welcome banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-primary-600 via-purple-600 to-indigo-600 p-6 text-white">
        <div className="absolute right-0 top-0 w-64 h-full opacity-10">
          <div className="absolute right-[-30px] top-[-30px] w-48 h-48 rounded-full bg-white" />
          <div className="absolute right-24 bottom-[-20px] w-32 h-32 rounded-full bg-white" />
        </div>
        <div className="relative z-10">
          <p className="text-white/70 text-sm font-medium">Good to see you back,</p>
          <h2 className="text-3xl font-bold mt-0.5">{user?.firstName} {user?.lastName} 👋</h2>
          <p className="text-white/70 text-sm mt-2">Keep up the great work on your learning journey!</p>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard label="Enrolled Courses"    value={enrollments.length}          icon={BookOpen}     colorIndex={0} />
        <StatCard label="Assignments Done"    value={submittedAssignments}         icon={Trophy}       colorIndex={1} />
        <StatCard label="Pending"             value={pending}                      icon={ClipboardList} colorIndex={2} />
        <StatCard label="Avg. Score"          value={`${Math.round(avgScore)}%`}   icon={TrendingUp}   colorIndex={3} />
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* Enrolled courses */}
        <div className="xl:col-span-2">
          <div className="flex items-center justify-between mb-4">
            <h2 className="section-title">My Enrolled Courses</h2>
            <Link to="/student/courses" className="text-sm text-primary-600 font-medium hover:underline">View all →</Link>
          </div>
          {enrollments.length === 0 ? (
            <div className="card text-center py-12 text-slate-400">
              <BookOpen size={40} className="mx-auto mb-3 text-slate-300" />
              <p>No courses enrolled yet.</p>
              <Link to="/student/courses" className="btn-primary mt-3 inline-flex text-sm">Browse Courses</Link>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {enrollments.slice(0, 4).map((e, i) => (
                <Link
                  key={e.enrollmentId}
                  to={`/student/courses/${e.courseId}`}
                  className="card hover:shadow-md transition-shadow flex items-center gap-3 group"
                >
                  <div className="w-12 h-12 rounded-xl bg-primary-100 flex items-center justify-center flex-shrink-0">
                    <BookOpen size={20} className="text-primary-600" />
                  </div>
                  <div className="min-w-0">
                    <p className="font-semibold text-slate-800 text-sm truncate group-hover:text-primary-600 transition-colors">
                      {e.courseName || `Course ${i + 1}`}
                    </p>
                    <p className="text-xs text-slate-400">Active Enrollment</p>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>

        {/* Score chart */}
        <div className="card flex flex-col items-center gap-4">
          <h3 className="section-title self-start">Average Score</h3>
          <div className="w-full h-44">
            <ResponsiveContainer width="100%" height="100%">
              <RadialBarChart cx="50%" cy="50%" innerRadius="60%" outerRadius="80%" data={radialData} startAngle={180} endAngle={-180}>
                <RadialBar dataKey="value" cornerRadius={10} />
                <Tooltip formatter={(v) => [`${v}%`, 'Score']} />
              </RadialBarChart>
            </ResponsiveContainer>
          </div>
          <div className="text-center">
            <p className="text-4xl font-bold text-slate-900">{Math.round(avgScore)}%</p>
            <p className="text-sm text-slate-400 mt-1">Overall performance</p>
          </div>
          <div className={clsx('badge text-sm px-4 py-1.5', scoreBg(avgScore))}>
            {avgScore >= 80 ? 'Excellent' : avgScore >= 60 ? 'Good' : 'Needs Improvement'}
          </div>
        </div>
      </div>

      {/* Analytics progress */}
      {analytics.length > 0 && (
        <div className="card">
          <h3 className="section-title mb-4">Learning Activity</h3>
          <div className="space-y-3">
            {analytics.slice(0, 5).map((p, i) => (
              <div key={i} className="flex items-center gap-4">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-sm font-medium text-slate-700 truncate capitalize">
                      {p.eventType?.toLowerCase().replace(/_/g, ' ') ?? 'Activity'}
                    </span>
                    {p.score != null && (
                      <span className="text-xs text-slate-400 ml-2 flex-shrink-0">
                        {Math.round(p.score)}%
                      </span>
                    )}
                  </div>
                  <ProgressBar value={p.score != null ? Math.round(p.score) : 100} />
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
