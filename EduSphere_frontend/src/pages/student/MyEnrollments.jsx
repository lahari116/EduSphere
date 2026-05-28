import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { enrollmentService } from '../../services/enrollmentService'
import { courseService } from '../../services/courseService'
import { useAuth } from '../../context/AuthContext'
import { PageLoader } from '../../components/common/LoadingSpinner'
import Badge from '../../components/common/Badge'
import Modal from '../../components/common/Modal'
import { BookOpen, ArrowRight, ClipboardList, Trash2 } from 'lucide-react'
import { formatDate } from '../../utils/helpers'
import toast from 'react-hot-toast'

export default function MyEnrollments() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [unenrollTarget, setUnenrollTarget] = useState(null)

  const { data: enrollData, isLoading } = useQuery({
    queryKey: ['student-enrollments', user?.userId],
    queryFn: () => enrollmentService.getStudentEnrollments(user.userId),
    enabled: !!user?.userId,
  })

  const { data: coursesData } = useQuery({
    queryKey: ['all-courses'],
    queryFn: () => courseService.getAll(),
  })

  const unenrollMutation = useMutation({
    mutationFn: (enrollmentId) => enrollmentService.unenroll(enrollmentId),
    onSuccess: () => {
      toast.success('Unenrolled successfully')
      qc.invalidateQueries({ queryKey: ['student-enrollments', user?.userId] })
      setUnenrollTarget(null)
    },
    onError: () => toast.error('Failed to unenroll'),
  })

  if (isLoading) return <PageLoader />

  const enrollments = enrollData?.data?.data || []
  const allCourses = coursesData?.data?.data || []
  const courseMap = Object.fromEntries(allCourses.map((c) => [c.courseId, c]))

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="page-title">My Enrollments</h1>
        <p className="text-slate-500 text-sm mt-0.5">
          {enrollments.length} course{enrollments.length !== 1 ? 's' : ''} enrolled
        </p>
      </div>

      {enrollments.length === 0 ? (
        <div className="card text-center py-20">
          <ClipboardList size={52} className="mx-auto text-slate-200 mb-4" />
          <p className="text-slate-500 font-medium">No enrollments yet.</p>
          <button onClick={() => navigate('/student/courses')} className="btn-primary mt-5 mx-auto">
            Browse &amp; Enroll
          </button>
        </div>
      ) : (
        <div className="space-y-3">
          {enrollments.map((e) => {
            const course = courseMap[e.courseId]
            return (
              <div key={e.enrollmentId} className="card flex items-center gap-4 hover:shadow-soft transition-shadow">
                <div className="w-11 h-11 rounded-xl bg-primary-100 flex items-center justify-center flex-shrink-0">
                  <BookOpen size={20} className="text-primary-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-slate-900 truncate">
                    {course?.courseName ?? `Course ${e.courseId?.slice(0, 8)}…`}
                  </p>
                  <div className="flex items-center gap-2 mt-1 flex-wrap">
                    {course?.courseCode && <Badge variant="purple">{course.courseCode}</Badge>}
                    <Badge variant={e.status === 'ACTIVE' ? 'green' : 'slate'}>{e.status ?? 'ACTIVE'}</Badge>
                    <span className="text-xs text-slate-400">Enrolled {formatDate(e.enrolledAt)}</span>
                  </div>
                </div>
                <div className="flex items-center gap-2 flex-shrink-0">
                  <button
                    onClick={() => navigate(`/student/courses/${e.courseId}`)}
                    className="btn-primary text-xs py-1.5 px-3 flex items-center gap-1.5"
                  >
                    Go to Course <ArrowRight size={13} />
                  </button>
                  <button
                    onClick={() => setUnenrollTarget(e)}
                    className="p-1.5 text-rose-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                    title="Unenroll"
                  >
                    <Trash2 size={15} />
                  </button>
                </div>
              </div>
            )
          })}
        </div>
      )}

      <div className="text-center pt-2">
        <button onClick={() => navigate('/student/courses')} className="btn-secondary">
          Browse More Courses
        </button>
      </div>

      <Modal open={!!unenrollTarget} onClose={() => setUnenrollTarget(null)} title="Confirm Unenroll" size="sm">
        <p className="text-slate-600 text-sm mb-6">
          Are you sure you want to unenroll from <strong>{courseMap[unenrollTarget?.courseId]?.courseName || 'this course'}</strong>?
        </p>
        <div className="flex gap-3">
          <button onClick={() => setUnenrollTarget(null)} className="btn-secondary flex-1">Cancel</button>
          <button
            onClick={() => unenrollMutation.mutate(unenrollTarget.enrollmentId)}
            disabled={unenrollMutation.isPending}
            className="flex-1 bg-rose-500 hover:bg-rose-600 text-white rounded-xl px-4 py-2 text-sm font-medium transition-colors"
          >
            {unenrollMutation.isPending ? 'Unenrolling…' : 'Unenroll'}
          </button>
        </div>
      </Modal>
    </div>
  )
}
