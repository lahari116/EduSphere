import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { adminService } from '../../services/adminService'
import { departmentService } from '../../services/departmentService'
import { PageLoader } from '../../components/common/LoadingSpinner'
import Badge from '../../components/common/Badge'
import Modal from '../../components/common/Modal'
import { Search, Trash2, Upload, Users, Building2 } from 'lucide-react'
import { getInitials } from '../../utils/helpers'
import toast from 'react-hot-toast'
import clsx from 'clsx'

const ROLE_BADGE = { STUDENT: 'blue', INSTRUCTOR: 'green', COORDINATOR: 'amber', ADMIN: 'rose' }
const ROLE_GRADIENT = { STUDENT: 'from-blue-400 to-indigo-500', INSTRUCTOR: 'from-emerald-400 to-teal-500', COORDINATOR: 'from-amber-400 to-orange-500', ADMIN: 'from-rose-400 to-pink-500' }

export default function UserManagement() {
  const qc = useQueryClient()
  const [search, setSearch] = useState('')
  const [roleFilter, setRoleFilter] = useState('ALL')
  const [uploadModal, setUploadModal] = useState(false)
  const [file, setFile] = useState(null)
  const [deleteConfirm, setDeleteConfirm] = useState(null)
  const [importResult, setImportResult] = useState(null)

  const { data, isLoading } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => adminService.getUsers(),
  })

  const { data: deptData } = useQuery({
    queryKey: ['departments'],
    queryFn: () => departmentService.getAll(),
  })

  const deptMap = (deptData?.data?.data || []).reduce((acc, d) => {
    acc[d.departmentId] = d.departmentName
    return acc
  }, {})

  const deleteMutation = useMutation({
    mutationFn: (id) => adminService.deleteUser(id),
    onSuccess: () => {
      toast.success('User deleted')
      setDeleteConfirm(null)
      qc.invalidateQueries({ queryKey: ['admin-users'] })
    },
  })

  const uploadMutation = useMutation({
    mutationFn: (file) => adminService.uploadUsers(file),
    onSuccess: (res) => {
      const data = res?.data?.data || {}
      const created = data.totalCreated ?? 0
      const updated = data.totalUpdated ?? 0
      const errCount = data.totalErrors ?? 0
      const notifFails = data.totalNotificationFailures ?? 0
      setImportResult(data)
      if (created > 0 || updated > 0) {
        const suffix = notifFails > 0 ? ` (${notifFails} email(s) failed)` : ''
        toast.success(`Import complete: ${created} created, ${updated} updated${errCount > 0 ? `, ${errCount} errors` : ''}${suffix}`)
      } else if (errCount > 0) {
        toast.error(`Import failed: ${errCount} error(s). No users added.`)
      } else {
        toast.success('No changes — all rows already up to date.')
      }
      setFile(null)
      qc.invalidateQueries({ queryKey: ['admin-users'] })
    },
    onError: () => {
      setImportResult(null)
    },
  })

  if (isLoading) return <PageLoader />

  const users = (data?.data?.data || []).filter((u) => {
    const matchSearch = `${u.firstName} ${u.lastName} ${u.email}`.toLowerCase().includes(search.toLowerCase())
    const matchRole = roleFilter === 'ALL' || u.role === roleFilter
    return matchSearch && matchRole
  })

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="page-title">User Management</h1>
          <p className="text-slate-500 text-sm mt-0.5">{data?.data?.data?.length ?? 0} total users</p>
        </div>
        <div className="flex gap-2">
          <button onClick={() => setUploadModal(true)} className="btn-secondary">
            <Upload size={16} /> Import Excel
          </button>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1 max-w-xs">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search users…" className="input pl-9" />
        </div>
        <div className="flex gap-1 bg-slate-100 p-1 rounded-xl">
          {['ALL', 'STUDENT', 'INSTRUCTOR', 'COORDINATOR', 'ADMIN'].map((r) => (
            <button
              key={r}
              onClick={() => setRoleFilter(r)}
              className={clsx('px-3 py-1.5 rounded-lg text-xs font-medium transition-all',
                roleFilter === r ? 'bg-white text-primary-700 shadow-sm' : 'text-slate-500 hover:text-slate-700'
              )}
            >
              {r === 'ALL' ? 'All' : r.charAt(0) + r.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      {/* Users grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        {users.map((u) => (
          <div key={u.userId} className="card hover:shadow-soft transition-shadow group">
            <div className="flex items-start justify-between mb-3">
              <div className={clsx('w-11 h-11 rounded-xl flex items-center justify-center text-white font-bold text-sm bg-gradient-to-br', ROLE_GRADIENT[u.role] || 'from-slate-400 to-slate-500')}>
                {getInitials(u.firstName, u.lastName)}
              </div>
              <button
                onClick={() => setDeleteConfirm(u)}
                className="p-1.5 text-slate-300 hover:text-rose-500 hover:bg-rose-50 rounded-lg transition-colors opacity-0 group-hover:opacity-100"
              >
                <Trash2 size={15} />
              </button>
            </div>
            <p className="font-semibold text-slate-900 text-sm">{u.firstName} {u.lastName}</p>
            <p className="text-xs text-slate-400 truncate mt-0.5">{u.email}</p>
            {u.departmentId && deptMap[u.departmentId] && (
              <p className="text-xs text-primary-600 mt-1 flex items-center gap-1">
                <Building2 size={11} />
                {deptMap[u.departmentId]}
              </p>
            )}
            <div className="flex items-center justify-between mt-3">
              <Badge variant={ROLE_BADGE[u.role] || 'slate'}>{u.role}</Badge>
              <Badge variant={u.active ? 'green' : 'slate'}>{u.active ? 'Active' : 'Inactive'}</Badge>
            </div>
            {u.studentOrEmployeeId && (
              <p className="text-xs text-slate-400 mt-2 font-mono">{u.studentOrEmployeeId}</p>
            )}
          </div>
        ))}
        {users.length === 0 && (
          <div className="col-span-full card text-center py-16 text-slate-400">
            <Users size={48} className="mx-auto mb-4 text-slate-200" />
            <p>No users found</p>
          </div>
        )}
      </div>

      {/* Upload modal */}
      <Modal open={uploadModal} onClose={() => { setUploadModal(false); setImportResult(null) }} title="Import Users via Excel">
        <div className="space-y-4">
          {!importResult ? (
            <>
              <div className="bg-primary-50 rounded-xl p-4 text-sm text-primary-700">
                <p className="font-medium mb-1">Excel format required:</p>
                <p className="text-xs text-primary-600 font-mono">firstName | lastName | email | role | departmentCode | studentOrEmployeeId</p>
                <p className="text-xs text-primary-500 mt-1">Existing users will be updated if any field changed. New users receive a welcome email with their temporary password. Departments are auto-created if they don't exist.</p>
              </div>
              <div className="border-2 border-dashed border-primary-200 rounded-xl p-8 text-center">
                <Upload size={28} className="mx-auto text-primary-300 mb-2" />
                <p className="text-sm text-slate-500">Select your Excel (.xlsx) file</p>
                <input type="file" accept=".xlsx,.xls" onChange={(e) => setFile(e.target.files[0])} className="mt-3 text-sm text-slate-600 file:mr-3 file:py-1.5 file:px-4 file:rounded-lg file:border-0 file:bg-primary-100 file:text-primary-700 file:font-medium cursor-pointer" />
              </div>
              {file && <p className="text-sm text-emerald-600 font-medium">✓ {file.name}</p>}
              <div className="flex gap-3">
                <button onClick={() => setUploadModal(false)} className="btn-secondary flex-1">Cancel</button>
                <button onClick={() => uploadMutation.mutate(file)} disabled={!file || uploadMutation.isPending} className="btn-primary flex-1">
                  {uploadMutation.isPending ? 'Importing…' : 'Import'}
                </button>
              </div>
            </>
          ) : (
            <>
              <div className="space-y-2">
                <div className="flex items-center justify-between p-3 bg-emerald-50 rounded-lg text-sm">
                  <span className="text-emerald-700 font-medium">New users created</span>
                  <span className="font-bold text-emerald-800">{importResult.totalCreated ?? 0}</span>
                </div>
                <div className="flex items-center justify-between p-3 bg-blue-50 rounded-lg text-sm">
                  <span className="text-blue-700 font-medium">Existing users updated</span>
                  <span className="font-bold text-blue-800">{importResult.totalUpdated ?? 0}</span>
                </div>
                {(importResult.totalErrors ?? 0) > 0 && (
                  <div className="p-3 bg-rose-50 rounded-lg text-sm">
                    <p className="text-rose-700 font-medium mb-2">Row Errors ({importResult.totalErrors})</p>
                    <ul className="space-y-1 max-h-32 overflow-y-auto">
                      {(importResult.errorRows || []).map((err, idx) => (
                        <li key={idx} className="text-xs text-rose-600">Row {err.row} ({err.email}): {err.reason}</li>
                      ))}
                    </ul>
                  </div>
                )}
                {(importResult.totalNotificationFailures ?? 0) > 0 && (
                  <div className="p-3 bg-amber-50 rounded-lg text-sm">
                    <p className="text-amber-700 font-medium mb-2">Email Delivery Failures ({importResult.totalNotificationFailures})</p>
                    <ul className="space-y-1 max-h-24 overflow-y-auto">
                      {(importResult.notificationFailures || []).map((email, idx) => (
                        <li key={idx} className="text-xs text-amber-600">{email}</li>
                      ))}
                    </ul>
                    <p className="text-xs text-amber-500 mt-1">Users were created/updated but welcome emails could not be sent. Check notification service logs.</p>
                  </div>
                )}
              </div>
              <button onClick={() => { setUploadModal(false); setImportResult(null) }} className="btn-primary w-full">Done</button>
            </>
          )}
        </div>
      </Modal>

      {/* Delete confirm modal */}
      <Modal open={!!deleteConfirm} onClose={() => setDeleteConfirm(null)} title="Delete User" size="sm">
        <div className="space-y-4">
          <p className="text-slate-600 text-sm">
            Are you sure you want to delete <strong>{deleteConfirm?.firstName} {deleteConfirm?.lastName}</strong>? This action cannot be undone.
          </p>
          <div className="flex gap-3">
            <button onClick={() => setDeleteConfirm(null)} className="btn-secondary flex-1">Cancel</button>
            <button onClick={() => deleteMutation.mutate(deleteConfirm?.userId)} disabled={deleteMutation.isPending} className="btn-danger flex-1">
              {deleteMutation.isPending ? 'Deleting…' : 'Delete User'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
