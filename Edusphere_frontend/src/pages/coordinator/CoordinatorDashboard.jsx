import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../../context/AuthContext'
import { courseService } from '../../services/courseService'
import { enrollmentService } from '../../services/enrollmentService'
import { analyticsService } from '../../services/analyticsService'
import StatCard from '../../components/dashboard/StatCard'
import { PageLoader } from '../../components/common/LoadingSpinner'
import { BookOpen, Users, TrendingUp, FolderKanban, ArrowRight, Clock, Bell } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import Badge from '../../components/common/Badge'

export default function CoordinatorDashboard() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const { data: coursesData, isLoading } = useQuery({
    queryKey: ['courses'],
    queryFn: () => courseService.getAll(),
  })

  const { data: kpiData } = useQuery({
    queryKey: ['kpis'],
    queryFn: () => analyticsService.getKpis(),
  })

  const { data: pendingData } = useQuery({
    queryKey: ['pending-enrollments'],
    queryFn: () => enrollmentService.getPendingRequests(),
  })

  if (isLoading) return <PageLoader />

  const courses = coursesData?.data?.data || []
  const kpis    = kpiData?.data?.data    || {}
  const pendingRequests = pendingData?.data?.data || []

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Welcome banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-amber-500 via-orange-500 to-rose-500 p-6 text-white">
        <div className="absolute right-0 top-0 w-64 h-full opacity-10">
          <div className="absolute right-[-30px] top-[-30px] w-48 h-48 rounded-full bg-white" />
          <div className="absolute right-24 bottom-[-20px] w-32 h-32 rounded-full bg-white" />
        </div>
        <div className="relative z-10">
          <p className="text-white/70 text-sm font-medium">Welcome,</p>
          <h2 className="text-3xl font-bold mt-0.5">Coordinator {user?.firstName} {user?.lastName}</h2>
          <p className="text-white/70 text-sm mt-2">Manage course assignments, departments, and enrollments.</p>
        </div>
      </div>

      {/* Pending requests alert */}
      {pendingRequests.length > 0 && (
        <div
          onClick={() => navigate('/coordinator/enrollments')}
          className="flex items-center justify-between gap-4 bg-amber-50 border border-amber-200 rounded-2xl px-5 py-4 cursor-pointer hover:bg-amber-100 transition-colors"
        >
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-amber-100 flex items-center justify-center flex-shrink-0">
              <Bell size={18} className="text-amber-600" />
            </div>
            <div>
              <p className="font-semibold text-slate-800 text-sm">
                {pendingRequests.length} Pending Enrollment Request{pendingRequests.length !== 1 ? 's' : ''}
              </p>
              <p className="text-xs text-slate-500 mt-0.5">Students are waiting for your approval.</p>
            </div>
          </div>
          <div className="flex items-center gap-2 flex-shrink-0">
            <span className="px-2.5 py-1 rounded-lg bg-amber-500 text-white text-sm font-bold">
              {pendingRequests.length}
            </span>
            <ArrowRight size={16} className="text-amber-600" />
          </div>
        </div>
      )}

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard label="Total Courses"          value={courses.length}                 icon={BookOpen}     colorIndex={3} />
        <StatCard label="Total Students"         value={kpis.totalStudents ?? '—'}      icon={Users}        colorIndex={0} />
        <StatCard label="Pending Requests"       value={pendingRequests.length}         icon={Clock}        colorIndex={2} />
        <StatCard label="Completion Rate"        value={`${kpis.completionRate ?? 0}%`} icon={TrendingUp}   colorIndex={1} />
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* Managed courses list */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="section-title">Managed Courses</h3>
            <button
              onClick={() => navigate('/coordinator/courses')}
              className="text-sm text-primary-600 hover:underline font-medium flex items-center gap-1"
            >
              Manage <ArrowRight size={14} />
            </button>
          </div>
          <div className="space-y-3">
            {courses.slice(0, 6).map((c) => (
              <div key={c.courseId} className="flex items-center gap-3 p-3 rounded-xl hover:bg-primary-50 transition-colors">
                <div className="w-8 h-8 rounded-lg bg-amber-100 flex items-center justify-center flex-shrink-0">
                  <BookOpen size={14} className="text-amber-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-slate-800 truncate">{c.courseName}</p>
                  <p className="text-xs text-slate-400">{c.courseCode}</p>
                </div>
                <Badge variant="purple">{c.courseCode}</Badge>
              </div>
            ))}
            {courses.length === 0 && (
              <p className="text-sm text-slate-400 text-center py-6">No courses found.</p>
            )}
          </div>
        </div>

        {/* Course summary */}
        <div className="card">
          <h3 className="section-title mb-4">Quick Overview</h3>
          <div className="space-y-3">
            <div className="flex items-center justify-between p-4 bg-primary-50 rounded-xl">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-primary-500" />
                <span className="text-sm font-medium text-slate-700">Total Courses</span>
              </div>
              <span className="text-xl font-bold text-primary-600">{courses.length}</span>
            </div>
            <div className="flex items-center justify-between p-4 bg-amber-50 rounded-xl">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-amber-400" />
                <span className="text-sm font-medium text-slate-700">Completion Rate</span>
              </div>
              <span className="text-xl font-bold text-amber-600">{kpis.completionRate ?? 0}%</span>
            </div>
            <div className="flex items-center justify-between p-4 bg-rose-50 rounded-xl">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-rose-400" />
                <span className="text-sm font-medium text-slate-700">Pending Requests</span>
              </div>
              <span className="text-xl font-bold text-rose-600">{pendingRequests.length}</span>
            </div>
            <div className="flex items-center justify-between p-4 bg-emerald-50 rounded-xl">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-emerald-500" />
                <span className="text-sm font-medium text-slate-700">Total Students</span>
              </div>
              <span className="text-xl font-bold text-emerald-600">{kpis.totalStudents ?? '—'}</span>
            </div>
          </div>

          {/* Quick actions */}
          <div className="mt-5 pt-4 border-t border-slate-100 space-y-2">
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-3">Quick Actions</p>
            <button
              onClick={() => navigate('/coordinator/enrollments')}
              className="w-full flex items-center justify-between px-4 py-3 bg-slate-50 hover:bg-amber-50 rounded-xl transition-colors group"
            >
              <span className="text-sm font-medium text-slate-700">
                Review Pending Requests
                {pendingRequests.length > 0 && (
                  <span className="ml-2 px-1.5 py-0.5 rounded bg-amber-500 text-white text-xs font-bold">
                    {pendingRequests.length}
                  </span>
                )}
              </span>
              <ArrowRight size={15} className="text-slate-400 group-hover:text-amber-600 transition-colors" />
            </button>
            <button
              onClick={() => navigate('/coordinator/courses')}
              className="w-full flex items-center justify-between px-4 py-3 bg-slate-50 hover:bg-primary-50 rounded-xl transition-colors group"
            >
              <span className="text-sm font-medium text-slate-700">Upload Syllabi</span>
              <ArrowRight size={15} className="text-slate-400 group-hover:text-primary-600 transition-colors" />
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
