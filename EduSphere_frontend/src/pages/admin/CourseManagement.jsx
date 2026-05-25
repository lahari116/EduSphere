import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { courseService } from '../../services/courseService'
import { PageLoader } from '../../components/common/LoadingSpinner'
import Modal from '../../components/common/Modal'
import Badge from '../../components/common/Badge'
import { Search, Plus, Trash2, Edit2, BookOpen } from 'lucide-react'
import toast from 'react-hot-toast'

const emptyCourse = { courseName: '', courseCode: '', description: '' }

// CourseForm must live OUTSIDE AdminCourseManagement so React never remounts it on re-render
function CourseForm({ value, onChange }) {
  return (
    <div className="space-y-3">
      <div>
        <label className="label">Course Name</label>
        <input
          required
          value={value.courseName}
          onChange={(e) => onChange({ ...value, courseName: e.target.value })}
          placeholder="Advanced Java Programming"
          className="input"
        />
      </div>
      <div>
        <label className="label">Course Code</label>
        <input
          required
          value={value.courseCode}
          onChange={(e) => onChange({ ...value, courseCode: e.target.value })}
          placeholder="CS-301"
          className="input"
        />
      </div>
      <div>
        <label className="label">Description</label>
        <textarea
          value={value.description}
          onChange={(e) => onChange({ ...value, description: e.target.value })}
          rows={3}
          placeholder="Brief course description…"
          className="input resize-none"
        />
      </div>
    </div>
  )
}

export default function AdminCourseManagement() {
  const qc = useQueryClient()
  const [search, setSearch] = useState('')
  const [createModal, setCreateModal] = useState(false)
  const [editModal, setEditModal] = useState(null)
  const [deleteConfirm, setDeleteConfirm] = useState(null)
  const [form, setForm] = useState(emptyCourse)

  const { data, isLoading } = useQuery({
    queryKey: ['courses'],
    queryFn: () => courseService.getAll(),
  })

  const createMutation = useMutation({
    mutationFn: (data) => courseService.create(data),
    onSuccess: () => {
      toast.success('Course created!')
      setCreateModal(false)
      setForm(emptyCourse)
      qc.invalidateQueries({ queryKey: ['courses'] })
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => courseService.update(id, data),
    onSuccess: () => {
      toast.success('Course updated!')
      setEditModal(null)
      qc.invalidateQueries({ queryKey: ['courses'] })
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => courseService.delete(id),
    onSuccess: () => {
      toast.success('Course deleted')
      setDeleteConfirm(null)
      qc.invalidateQueries({ queryKey: ['courses'] })
    },
  })

  if (isLoading) return <PageLoader />

  const courses = (data?.data?.data || []).filter((c) =>
    c.courseName?.toLowerCase().includes(search.toLowerCase()) ||
    c.courseCode?.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="page-title">Course Management</h1>
          <p className="text-slate-500 text-sm mt-0.5">{courses.length} courses</p>
        </div>
        <div className="flex gap-3">
          <div className="relative max-w-xs">
            <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search courses…"
              className="input pl-9"
            />
          </div>
          <button onClick={() => { setForm(emptyCourse); setCreateModal(true) }} className="btn-primary">
            <Plus size={16} /> New Course
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {courses.map((c) => (
          <div key={c.courseId} className="card hover:shadow-soft transition-shadow group">
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-primary-100 flex items-center justify-center flex-shrink-0">
                  <BookOpen size={18} className="text-primary-600" />
                </div>
                <div>
                  <p className="font-semibold text-slate-900 text-sm leading-tight">{c.courseName}</p>
                  <Badge variant="purple" className="mt-0.5">{c.courseCode}</Badge>
                </div>
              </div>
              <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                <button
                  onClick={() => {
                    setEditModal(c)
                    setForm({ courseName: c.courseName, courseCode: c.courseCode, description: c.description || '' })
                  }}
                  className="p-1.5 text-slate-400 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
                >
                  <Edit2 size={14} />
                </button>
                <button
                  onClick={() => setDeleteConfirm(c)}
                  className="p-1.5 text-slate-400 hover:text-rose-500 hover:bg-rose-50 rounded-lg transition-colors"
                >
                  <Trash2 size={14} />
                </button>
              </div>
            </div>
            <p className="text-xs text-slate-400 line-clamp-2">{c.description || 'No description provided.'}</p>
          </div>
        ))}
        {courses.length === 0 && (
          <div className="col-span-full card text-center py-16 text-slate-400">
            <BookOpen size={48} className="mx-auto mb-4 text-slate-200" />
            <p>No courses found</p>
          </div>
        )}
      </div>

      {/* Create modal */}
      <Modal open={createModal} onClose={() => setCreateModal(false)} title="Create New Course" size="lg">
        <div className="space-y-4">
          <CourseForm value={form} onChange={setForm} />
          <div className="flex gap-3 pt-1">
            <button onClick={() => setCreateModal(false)} className="btn-secondary flex-1">Cancel</button>
            <button
              onClick={() => createMutation.mutate(form)}
              disabled={!form.courseName || !form.courseCode || createMutation.isPending}
              className="btn-primary flex-1"
            >
              {createMutation.isPending ? 'Creating…' : 'Create Course'}
            </button>
          </div>
        </div>
      </Modal>

      {/* Edit modal */}
      <Modal open={!!editModal} onClose={() => setEditModal(null)} title="Edit Course" size="lg">
        <div className="space-y-4">
          <CourseForm value={form} onChange={setForm} />
          <div className="flex gap-3 pt-1">
            <button onClick={() => setEditModal(null)} className="btn-secondary flex-1">Cancel</button>
            <button
              onClick={() => updateMutation.mutate({ id: editModal?.courseId, data: form })}
              disabled={updateMutation.isPending}
              className="btn-primary flex-1"
            >
              {updateMutation.isPending ? 'Saving…' : 'Save Changes'}
            </button>
          </div>
        </div>
      </Modal>

      {/* Delete confirm */}
      <Modal open={!!deleteConfirm} onClose={() => setDeleteConfirm(null)} title="Delete Course" size="sm">
        <div className="space-y-4">
          <p className="text-slate-600 text-sm">
            Delete <strong>{deleteConfirm?.courseName}</strong>? This action cannot be undone.
          </p>
          <div className="flex gap-3">
            <button onClick={() => setDeleteConfirm(null)} className="btn-secondary flex-1">Cancel</button>
            <button
              onClick={() => deleteMutation.mutate(deleteConfirm?.courseId)}
              disabled={deleteMutation.isPending}
              className="btn-danger flex-1"
            >
              {deleteMutation.isPending ? 'Deleting…' : 'Delete'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
