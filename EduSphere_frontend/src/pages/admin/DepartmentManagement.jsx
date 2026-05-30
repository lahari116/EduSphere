import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { departmentService } from '../../services/departmentService'
import { adminService } from '../../services/adminService'
import { PageLoader } from '../../components/common/LoadingSpinner'
import Badge from '../../components/common/Badge'
import Modal from '../../components/common/Modal'
import { Building2, ChevronDown, ChevronRight, Users, Search, Plus } from 'lucide-react'
import { getInitials } from '../../utils/helpers'
import toast from 'react-hot-toast'
import clsx from 'clsx'

const ROLE_BADGE = { STUDENT: 'blue', INSTRUCTOR: 'green', COORDINATOR: 'amber', ADMIN: 'rose' }
const ROLE_GRADIENT = { STUDENT: 'from-blue-400 to-indigo-500', INSTRUCTOR: 'from-emerald-400 to-teal-500', COORDINATOR: 'from-amber-400 to-orange-500', ADMIN: 'from-rose-400 to-pink-500' }

export default function DepartmentManagement() {
  const qc = useQueryClient()
  const [search, setSearch] = useState('')
  const [expanded, setExpanded] = useState({})
  const [createModal, setCreateModal] = useState(false)
  const [newDept, setNewDept] = useState({ departmentName: '', departmentCode: '', description: '' })

  const { data: deptData, isLoading: deptsLoading } = useQuery({
    queryKey: ['departments'],
    queryFn: () => departmentService.getAll(),
  })

  const { data: userData, isLoading: usersLoading } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => adminService.getUsers(),
  })

  const createDeptMutation = useMutation({
    mutationFn: (data) => departmentService.create(data),
    onSuccess: () => {
      toast.success('Department created!')
      setCreateModal(false)
      setNewDept({ departmentName: '', departmentCode: '', description: '' })
      qc.invalidateQueries({ queryKey: ['departments'] })
    },
    onError: (err) => toast.error(err?.response?.data?.message || 'Failed to create department'),
  })

  if (deptsLoading || usersLoading) return <PageLoader />

  const departments = deptData?.data?.data || []
  const allUsers = userData?.data?.data || []

  const usersByDept = allUsers.reduce((acc, u) => {
    const key = u.departmentId || '__none__'
    if (!acc[key]) acc[key] = []
    acc[key].push(u)
    return acc
  }, {})

  const filtered = departments.filter((d) =>
    `${d.departmentName} ${d.departmentCode}`.toLowerCase().includes(search.toLowerCase())
  )

  const toggle = (id) => setExpanded((prev) => ({ ...prev, [id]: !prev[id] }))

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="page-title">Departments</h1>
          <p className="text-slate-500 text-sm mt-0.5">
            {departments.length} department{departments.length !== 1 ? 's' : ''} · {allUsers.length} total users
          </p>
        </div>
        <button onClick={() => setCreateModal(true)} className="btn-primary flex items-center gap-1.5 whitespace-nowrap">
          <Plus size={15} /> New Department
        </button>
      </div>

      {/* Search */}
      <div className="relative max-w-xs">
        <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search departments…"
          className="input pl-9"
        />
      </div>

      {/* Unassigned users */}
      {(usersByDept['__none__'] || []).length > 0 && (
        <div className="card border border-slate-200">
          <button
            onClick={() => toggle('__none__')}
            className="flex items-center justify-between w-full text-left"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-slate-100 flex items-center justify-center">
                <Users size={18} className="text-slate-400" />
              </div>
              <div>
                <p className="font-semibold text-slate-700">No Department</p>
                <p className="text-xs text-slate-400">Users without a department assignment</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Badge variant="slate">{(usersByDept['__none__'] || []).length} user{(usersByDept['__none__'] || []).length !== 1 ? 's' : ''}</Badge>
              {expanded['__none__'] ? <ChevronDown size={16} className="text-slate-400" /> : <ChevronRight size={16} className="text-slate-400" />}
            </div>
          </button>
          {expanded['__none__'] && (
            <div className="mt-4 pt-4 border-t border-slate-100 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {(usersByDept['__none__'] || []).map((u) => (
                <UserChip key={u.userId} user={u} />
              ))}
            </div>
          )}
        </div>
      )}

      {/* Create department modal */}
      <Modal open={createModal} onClose={() => setCreateModal(false)} title="Create New Department">
        <div className="space-y-4">
          <div>
            <label className="label">Department Name</label>
            <input
              value={newDept.departmentName}
              onChange={(e) => setNewDept((p) => ({ ...p, departmentName: e.target.value }))}
              placeholder="e.g. Computer Science"
              className="input"
            />
          </div>
          <div>
            <label className="label">Department Code</label>
            <input
              value={newDept.departmentCode}
              onChange={(e) => setNewDept((p) => ({ ...p, departmentCode: e.target.value.toUpperCase() }))}
              placeholder="e.g. CS"
              className="input"
            />
          </div>
          <div>
            <label className="label">Description <span className="text-slate-400 font-normal">(optional)</span></label>
            <textarea
              value={newDept.description}
              onChange={(e) => setNewDept((p) => ({ ...p, description: e.target.value }))}
              placeholder="Brief description…"
              rows={3}
              className="input resize-none"
            />
          </div>
          <div className="flex gap-3">
            <button onClick={() => setCreateModal(false)} className="btn-secondary flex-1">Cancel</button>
            <button
              onClick={() => createDeptMutation.mutate(newDept)}
              disabled={!newDept.departmentName || !newDept.departmentCode || createDeptMutation.isPending}
              className="btn-primary flex-1"
            >
              {createDeptMutation.isPending ? 'Creating…' : 'Create'}
            </button>
          </div>
        </div>
      </Modal>

      {/* Department list */}
      <div className="space-y-3">
        {filtered.length === 0 && (
          <div className="card text-center py-16 text-slate-400">
            <Building2 size={48} className="mx-auto mb-4 text-slate-200" />
            <p>No departments found</p>
          </div>
        )}
        {filtered.map((dept) => {
          const deptUsers = usersByDept[dept.departmentId] || []
          const isOpen = expanded[dept.departmentId]
          return (
            <div key={dept.departmentId} className="card border border-slate-200 hover:border-primary-200 transition-colors">
              <button
                onClick={() => toggle(dept.departmentId)}
                className="flex items-center justify-between w-full text-left"
              >
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center">
                    <Building2 size={18} className="text-white" />
                  </div>
                  <div>
                    <p className="font-semibold text-slate-900">{dept.departmentName}</p>
                    <p className="text-xs text-slate-400 font-mono">{dept.departmentCode}</p>
                    {dept.description && (
                      <p className="text-xs text-slate-500 mt-0.5">{dept.description}</p>
                    )}
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <Badge variant={deptUsers.length > 0 ? 'blue' : 'slate'}>
                    {deptUsers.length} user{deptUsers.length !== 1 ? 's' : ''}
                  </Badge>
                  {isOpen ? <ChevronDown size={16} className="text-slate-400" /> : <ChevronRight size={16} className="text-slate-400" />}
                </div>
              </button>

              {isOpen && (
                <div className="mt-4 pt-4 border-t border-slate-100">
                  {deptUsers.length === 0 ? (
                    <p className="text-sm text-slate-400 text-center py-4">No users in this department</p>
                  ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                      {deptUsers.map((u) => (
                        <UserChip key={u.userId} user={u} />
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}

function UserChip({ user: u }) {
  return (
    <div className="flex items-center gap-2.5 p-2.5 rounded-xl bg-slate-50 hover:bg-slate-100 transition-colors">
      <div className={clsx('w-8 h-8 rounded-lg flex items-center justify-center text-white text-xs font-bold flex-shrink-0 bg-gradient-to-br', ROLE_GRADIENT[u.role] || 'from-slate-400 to-slate-500')}>
        {getInitials(u.firstName, u.lastName)}
      </div>
      <div className="min-w-0">
        <p className="text-sm font-medium text-slate-800 truncate">{u.firstName} {u.lastName}</p>
        <p className="text-xs text-slate-400 truncate">{u.email}</p>
      </div>
      <Badge variant={ROLE_BADGE[u.role] || 'slate'} className="flex-shrink-0 ml-auto">
        {u.role.charAt(0) + u.role.slice(1).toLowerCase()}
      </Badge>
    </div>
  )
}
