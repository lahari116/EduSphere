import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../../context/AuthContext'
import { enrollmentService } from '../../services/enrollmentService'
import { courseService } from '../../services/courseService'
import { PageLoader } from '../../components/common/LoadingSpinner'
import { Award, BookOpen, Calendar, CheckCircle, Download } from 'lucide-react'
import { formatDate } from '../../utils/helpers'

export default function MyCertifications() {
  const { user } = useAuth()

  const { data: enrollData, isLoading } = useQuery({
    queryKey: ['student-enrollments', user?.userId],
    queryFn: () => enrollmentService.getStudentEnrollments(user.userId),
    enabled: !!user?.userId,
  })

  const { data: allCoursesData } = useQuery({
    queryKey: ['all-courses'],
    queryFn: () => courseService.getAll(),
  })

  if (isLoading) return <PageLoader />

  const enrollments = enrollData?.data?.data || []
  const allCourses = allCoursesData?.data?.data || []
  const courseMap = Object.fromEntries(allCourses.map((c) => [c.courseId, c]))

  // Show enrollments where status is COMPLETED
  const completed = enrollments.filter((e) => e.status === 'COMPLETED')

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-amber-500 via-yellow-500 to-orange-500 p-6 text-white shadow-glow">
        <div className="absolute right-0 top-0 w-64 h-full opacity-10">
          <div className="absolute right-[-30px] top-[-30px] w-48 h-48 rounded-full bg-white" />
          <div className="absolute right-24 bottom-[-20px] w-32 h-32 rounded-full bg-white" />
        </div>
        <div className="relative z-10 flex items-start justify-between gap-4">
          <div>
            <p className="text-white/70 text-sm font-medium">Your achievements</p>
            <h2 className="text-3xl font-bold mt-0.5">My Certifications</h2>
            <p className="text-white/70 text-sm mt-2">
              {completed.length} certificate{completed.length !== 1 ? 's' : ''} earned
            </p>
          </div>
          <div className="w-14 h-14 rounded-2xl bg-white/20 backdrop-blur flex items-center justify-center flex-shrink-0">
            <Award size={28} className="text-white" />
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="card text-center">
          <div className="w-12 h-12 rounded-2xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center mx-auto mb-3">
            <Award size={22} className="text-amber-600 dark:text-amber-400" />
          </div>
          <p className="text-3xl font-bold" style={{ color: 'var(--text-primary)' }}>{completed.length}</p>
          <p className="text-sm mt-1" style={{ color: 'var(--text-muted)' }}>Certificates Earned</p>
        </div>
        <div className="card text-center">
          <div className="w-12 h-12 rounded-2xl bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center mx-auto mb-3">
            <BookOpen size={22} className="text-blue-600 dark:text-blue-400" />
          </div>
          <p className="text-3xl font-bold" style={{ color: 'var(--text-primary)' }}>{enrollments.length}</p>
          <p className="text-sm mt-1" style={{ color: 'var(--text-muted)' }}>Total Enrollments</p>
        </div>
        <div className="card text-center">
          <div className="w-12 h-12 rounded-2xl bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center mx-auto mb-3">
            <CheckCircle size={22} className="text-emerald-600 dark:text-emerald-400" />
          </div>
          <p className="text-3xl font-bold" style={{ color: 'var(--text-primary)' }}>
            {enrollments.length > 0 ? Math.round((completed.length / enrollments.length) * 100) : 0}%
          </p>
          <p className="text-sm mt-1" style={{ color: 'var(--text-muted)' }}>Completion Rate</p>
        </div>
      </div>

      {/* Certificates grid */}
      {completed.length === 0 ? (
        <div className="card text-center py-16" style={{ color: 'var(--text-muted)' }}>
          <Award size={56} className="mx-auto mb-4 opacity-20" />
          <p className="text-lg font-semibold" style={{ color: 'var(--text-primary)' }}>No certificates yet</p>
          <p className="text-sm mt-2">Complete a course to earn your first certificate!</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {completed.map((enrollment) => {
            const course = courseMap[enrollment.courseId]
            return (
              <div
                key={enrollment.enrollmentId}
                className="card group hover:shadow-md transition-all border border-transparent hover:border-amber-200 dark:hover:border-amber-800"
              >
                {/* Gold accent bar */}
                <div className="h-1.5 bg-gradient-to-r from-amber-400 via-yellow-400 to-orange-400 -mx-6 -mt-6 mb-5 rounded-t-2xl" />

                {/* Certificate icon */}
                <div className="flex items-center justify-between mb-4">
                  <div className="w-12 h-12 rounded-2xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center">
                    <Award size={24} className="text-amber-600 dark:text-amber-400" />
                  </div>
                  <span className="px-3 py-1 rounded-full text-xs font-semibold bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400 flex items-center gap-1">
                    <CheckCircle size={11} />
                    Completed
                  </span>
                </div>

                {/* Course info */}
                <h3 className="font-bold text-base mb-1 leading-snug" style={{ color: 'var(--text-primary)' }}>
                  {course?.courseName || `Course ${enrollment.courseId?.slice(0, 8)}`}
                </h3>
                {course?.courseCode && (
                  <p className="text-xs font-medium mb-3" style={{ color: 'var(--text-muted)' }}>
                    {course.courseCode}
                  </p>
                )}

                {/* Meta */}
                <div className="flex items-center gap-4 pt-3 border-t text-xs" style={{ borderColor: 'var(--border)', color: 'var(--text-muted)' }}>
                  <span className="flex items-center gap-1">
                    <Calendar size={11} />
                    {enrollment.enrolledAt ? formatDate(enrollment.enrolledAt) : 'Completed'}
                  </span>
                </div>

                {/* Download button — placeholder for when backend PDF generation is available */}
                <button
                  type="button"
                  className="mt-4 w-full flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-medium transition-all bg-amber-50 dark:bg-amber-900/20 text-amber-700 dark:text-amber-400 hover:bg-amber-100 dark:hover:bg-amber-900/30 border border-amber-200 dark:border-amber-800"
                  onClick={() => window.alert('Certificate PDF download will be available once the course completion email has been sent to you.')}
                >
                  <Download size={14} />
                  View Certificate
                </button>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
