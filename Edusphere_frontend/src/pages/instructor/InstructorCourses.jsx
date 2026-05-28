import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../../context/AuthContext'
import { courseService } from '../../services/courseService'
import { enrollmentService } from '../../services/enrollmentService'
import { PageLoader } from '../../components/common/LoadingSpinner'
import Badge from '../../components/common/Badge'
import { BookOpen, ArrowRight, Users, ClipboardList } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { formatDate } from '../../utils/helpers'

export default function InstructorCourses() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const { data: enrollData, isLoading } = useQuery({
    queryKey: ['instructor-enrollments', user?.userId],
    queryFn: () => enrollmentService.getInstructorEnrollments(user.userId),
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

  const myCourses = enrollments
    .map((e) => ({ enrollment: e, course: courseMap[e.courseId] }))
    .filter(({ course }) => !!course)

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="page-title">My Courses</h1>
        <p className="text-slate-500 text-sm mt-0.5">
          {myCourses.length} course{myCourses.length !== 1 ? 's' : ''} you are enrolled in as instructor
        </p>
      </div>

      {myCourses.length === 0 ? (
        <div className="card text-center py-16 text-slate-400">
          <BookOpen size={48} className="mx-auto mb-4 text-slate-200" />
          <p className="text-base font-medium">You are not assigned to any courses yet.</p>
          <p className="text-sm mt-1">Contact your coordinator to be enrolled in a course.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {myCourses.map(({ enrollment, course }) => (
            <div
              key={enrollment.enrollmentId}
              onClick={() => navigate(`/instructor/courses/${course.courseId}`)}
              className="card hover:shadow-md transition-all cursor-pointer group border border-transparent hover:border-emerald-200"
            >
              <div className="h-1.5 bg-gradient-to-r from-emerald-400 to-teal-500 -mx-6 -mt-6 mb-5 rounded-t-2xl" />
              <div className="flex items-start gap-3 mb-3">
                <div className="w-11 h-11 rounded-xl bg-emerald-100 flex items-center justify-center flex-shrink-0">
                  <BookOpen size={20} className="text-emerald-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-slate-900 truncate group-hover:text-emerald-700 transition-colors">
                    {course.courseName}
                  </p>
                  <Badge variant="green" className="mt-1">{course.courseCode}</Badge>
                </div>
                <ArrowRight size={16} className="text-slate-300 group-hover:text-emerald-500 transition-colors flex-shrink-0 mt-1" />
              </div>

              {course.description && (
                <p className="text-xs text-slate-400 line-clamp-2 mb-3">{course.description}</p>
              )}

              <div className="flex items-center gap-4 pt-3 border-t border-slate-100 text-xs text-slate-500">
                <span className="flex items-center gap-1">
                  <Users size={12} /> View Enrollments
                </span>
                <span className="flex items-center gap-1">
                  <ClipboardList size={12} /> Manage Assignments
                </span>
              </div>

              {course.completionDeadline && (
                <p className="text-xs text-slate-400 mt-2">
                  Deadline: {formatDate(course.completionDeadline)}
                </p>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
