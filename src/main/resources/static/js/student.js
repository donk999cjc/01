/**
 * 学生端页面逻辑（增强版）
 * 文件：js/student.js
 * 描述：学生学习台的Vue应用实例和业务逻辑
 */

new Vue({
    el: '#app',
    data() {
        return {
            currentTab: 'assignments',

            userInfo: {},
            profileForm: {
                realName: '',
                email: '',
                phone: ''
            },
            passwordForm: {
                oldPassword: '',
                newPassword: '',
                confirmPassword: ''
            },

            selAid: null,
            selectedFile: null,
            previewUrl: '',
            submitting: false,
            submitOk: false,

            assignments: [],
            submissions: [],
            teachers: [],
            loading: false,

            chart: null,

            chatMessages: [],
            chatInput: '',
            chatLoading: false,
            chatMode: 'normal',
            ragAvailable: false,
            lastUserMessage: ''
        }
    },
    computed: {
        tabTitle() {
            return {
                assignments: '作业列表',
                submit: '提交作业',
                results: '批改结果',
                analysis: '成绩分析',
                chat: 'AI 助手',
                profile: '个人信息',
                teachers: '教师信息'
            }[this.currentTab]
        },
        selAssignment() { return this.assignments.find(a => a.id === this.selAid) },
        gradedSubmissions() {
            return (this.submissions || []).filter(s => s.studentId === this.userInfo.studentId && s.status === 'graded')
        },
        recentSubs() {
            return (this.submissions || []).filter(s => s.studentId === this.userInfo.studentId).slice(-10).reverse()
        },
        myStats() {
            const mine = this.submissions.filter(s => s.studentId === this.userInfo.studentId && s.score != null)
            const g = mine.filter(s => s.status === 'graded')
            return {
                total: g.length,
                avgScore: g.length ? (g.reduce((a, b) => a + Number(b.score), 0) / g.length).toFixed(1) : 0,
                maxScore: g.length ? Math.max(...g.map(s => Number(s.score))) : null
            }
        }
    },
    created() {
        this.initUser()
        this.loadAll()
        this.checkRAGStatus()
    },
    mounted() {
        this.$nextTick(() => {
            if (document.getElementById('myChart')) {
                this.chart = echarts.init(document.getElementById('myChart'))
                window.addEventListener('resize', () => this.chart && this.chart.resize())
            }
        })
    },
    watch: {
        currentTab(v) {
            if (v === 'analysis') {
                this.$nextTick(() => this.renderMyChart())
            }
            if (v === 'chat') {
                this.$nextTick(() => {
                    this.autoResizeTextarea()
                })
            }
        }
    },
    methods: {
        initUser() {
            try {
                const userStr = localStorage.getItem('user')
                if (userStr) {
                    this.userInfo = JSON.parse(userStr)
                    this.profileForm.realName = this.userInfo.realName || ''
                    this.profileForm.email = this.userInfo.email || ''
                    this.profileForm.phone = this.userInfo.phone || ''
                }
            } catch (e) {
                console.error(e)
            }
        },

        switchTab(tab) {
            this.currentTab = tab
        },

        handleLogout() {
            if (confirm('确定要退出登录吗？')) {
                localStorage.removeItem('user')
                localStorage.removeItem('token')
                window.location.href = '/login.html'
            }
        },

        async loadAll() {
            this.loading = true
            try {
                const [r1, r2, r3] = await Promise.all([
                    fetch('/api/assignments').catch(() => ({ json: async () => [] })),
                    fetch('/api/submissions').catch(() => ({ json: async () => [] })),
                    fetch('/api/teachers').catch(() => ({ json: async () => [] }))
                ])
                this.assignments = (await r1.json()) || []
                this.submissions = (await r2.json()) || []
                this.teachers = (await r3.json()) || []
                if (this.currentTab === 'analysis') {
                    this.$nextTick(() => this.renderMyChart())
                }
            } catch (e) {
                console.error(e)
            } finally {
                this.loading = false
            }
        },
        statusText(s) { return { draft: '草稿', published: '已发布', closed: '已截止' }[s] || s },
        statusType(s) { return { draft: 'info', published: 'success', closed: 'danger' }[s] || 'info' },
        getAssignTitle(aid) { const a = this.assignments.find(x => x.id === aid); return a ? a.title : `作业${aid}` },
        getAssignTotal(aid) { const a = this.assignments.find(x => x.id === aid); return a ? a.totalScore : 100 },
        splitFeedback(text) { if (!text) return []; return text.split('\n').filter(s => s.trim()) },

        goSubmit(a) {
            this.selAid = a.id
            this.currentTab = 'submit'
            this.submitOk = false
        },

        onFileChange(e) {
            const f = e.target.files[0]
            if (f) this.readImg(f)
        },
        onDrop(e) {
            const f = e.dataTransfer.files[0]
            if (f && f.type.startsWith('image/')) this.readImg(f)
        },
        readImg(file) {
            this.selectedFile = file
            if (this.previewUrl) {
                URL.revokeObjectURL(this.previewUrl)
            }
            this.previewUrl = URL.createObjectURL(file)
        },
        clearImg() {
            this.selectedFile = null
            if (this.previewUrl) {
                URL.revokeObjectURL(this.previewUrl)
                this.previewUrl = ''
            }
            this.$refs.fileInput.value = ''
        },

        async doSubmit() {
            if (!this.selAid) {
                this.$message.warning('请选择作业')
                return
            }
            if (!this.selectedFile) {
                this.$message.warning('请选择要上传的作业图片')
                return
            }

            this.submitting = true
            try {
                const selectedAssignment = this.assignments.find(a => a.id === this.selAid)
                const assignmentId = selectedAssignment ? selectedAssignment.assignmentId : String(this.selAid)

                const formData = new FormData()
                formData.append('file', this.selectedFile)
                formData.append('studentId', this.userInfo.studentId)
                formData.append('assignmentId', assignmentId)

                const res = await fetch(`/api/assignments/${this.selAid}/submit`, {
                    method: 'POST',
                    body: formData
                })

                const data = await res.json()
                if (data.success) {
                    this.submitOk = true
                    this.$message.success('提交成功！')
                    this.loadAll()
                    this.selectedFile = null
                    this.previewUrl = ''
                } else {
                    this.$message.error(data.message || '提交失败')
                }
            } catch (e) {
                console.error(e)
                this.$message.error('网络错误')
            } finally {
                this.submitting = false
            }
        },

        renderMyChart() {
            if (!this.chart) return
            const sorted = [...this.assignments].sort((a, b) => new Date(a.createdAt || 0) - new Date(b.createdAt || 0))
            const xData = sorted.map(a => a.title || `作业${a.id}`)
            const scores = sorted.map(a => {
                const s = this.submissions.find(sub => String(sub.assignmentId) === String(a.id) && sub.studentId === this.userInfo.studentId)
                return s && s.score != null ? Number(s.score) : null
            })

            this.chart.setOption({
                tooltip: { trigger: 'axis' },
                grid: { left: '3%', right: '4%', bottom: '8%', top: '10%', containLabel: true },
                xAxis: { type: 'category', data: xData, axisLabel: { rotate: 25 } },
                yAxis: { type: 'value', name: '分数', min: 0, max: 100 },
                series: [{
                    name: '我的得分',
                    type: 'line',
                    smooth: true,
                    data: scores,
                    connectNulls: false,
                    lineStyle: { width: 3, color: '#67c23a' },
                    areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(103,194,58,0.25)' }, { offset: 1, color: 'rgba(103,194,58,0.02)' }]) },
                    markPoint: { data: [{ type: 'max', name: '最高' }, { type: 'min', name: '最低' }] },
                    markLine: { data: [{ type: 'average', name: '平均' }], lineStyle: { color: '#E6A23C', type: 'dashed' } }
                }]
            }, true)
        },

        async checkRAGStatus() {
            try {
                const res = await fetch('/api/rag/status')
                const data = await res.json()
                this.ragAvailable = data.vectorStoreAvailable || false
            } catch (e) {
                this.ragAvailable = false
            }
        },

        handleChatKeydown(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault()
                this.sendMessage()
            }
        },

        autoResizeTextarea() {
            this.$nextTick(() => {
                const textarea = this.$refs.chatTextarea
                if (textarea) {
                    textarea.style.height = 'auto'
                    textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
                }
            })
        },

        quickAsk(question) {
            this.chatInput = question
            this.sendMessage()
        },

        clearChat() {
            this.chatMessages = []
        },

        async sendMessage() {
            if (!this.chatInput.trim() || this.chatLoading) return

            const message = this.chatInput.trim()
            this.lastUserMessage = message
            this.chatMessages.push({ role: 'user', content: message })
            this.chatInput = ''
            this.chatLoading = true

            this.autoResizeTextarea()
            this.scrollToBottom()

            try {
                let data
                if (this.chatMode === 'rag') {
                    const res = await fetch('/api/rag/chat', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            message: message,
                            courseId: this.userInfo.courseId || ''
                        })
                    })
                    data = await res.json()

                    if (data.success && data.response) {
                        const aiMsg = { role: 'ai', content: data.response }
                        this.chatMessages.push(aiMsg)
                    } else {
                        this.chatMessages.push({ role: 'ai', content: '抱歉，知识库问答服务暂时不可用。' })
                    }
                } else {
                    const res = await fetch('/api/ai/chat', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            message: message,
                            systemPrompt: '你是一个专业的AI学习助手，专门帮助学生学习。你可以：\n1. 解答学习问题\n2. 提供作业辅导\n3. 解释复杂概念\n4. 给出学习建议\n5. 鼓励学生进步\n请用友好、鼓励的语气回答，用中文回复。'
                        })
                    })
                    data = await res.json()

                    if (data.success && data.response) {
                        this.chatMessages.push({ role: 'ai', content: data.response })
                    } else {
                        this.chatMessages.push({ role: 'ai', content: '抱歉，AI服务暂时不可用。请稍后再试。' })
                    }
                }
            } catch (e) {
                console.error('AI调用错误:', e)
                this.chatMessages.push({ role: 'ai', content: '网络错误，无法连接到AI服务。' })
            } finally {
                this.chatLoading = false
                this.scrollToBottom()
            }
        },

        async regenerate(msg) {
            if (this.chatLoading) return
            const idx = this.chatMessages.indexOf(msg)
            if (idx > -1) {
                this.chatMessages.splice(idx, 1)
            }
            if (this.lastUserMessage) {
                this.chatInput = this.lastUserMessage
                this.sendMessage()
            }
        },

        copyMessage(content) {
            if (navigator.clipboard) {
                navigator.clipboard.writeText(content).then(() => {
                    this.$message.success('已复制到剪贴板')
                })
            } else {
                const textarea = document.createElement('textarea')
                textarea.value = content
                document.body.appendChild(textarea)
                textarea.select()
                document.execCommand('copy')
                document.body.removeChild(textarea)
                this.$message.success('已复制到剪贴板')
            }
        },

        renderMarkdown(text) {
            if (!text) return ''
            let html = text
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')

            html = html.replace(/```(\w*)\n([\s\S]*?)```/g, function(m, lang, code) {
                return '<pre class="code-block"><code>' + code.trim() + '</code></pre>'
            })
            html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
            html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
            html = html.replace(/\*(.+?)\*/g, '<em>$1</em>')
            html = html.replace(/^### (.+)$/gm, '<h4>$1</h4>')
            html = html.replace(/^## (.+)$/gm, '<h3>$1</h3>')
            html = html.replace(/^# (.+)$/gm, '<h2>$1</h2>')
            html = html.replace(/^- (.+)$/gm, '<li>$1</li>')
            html = html.replace(/^(\d+)\. (.+)$/gm, '<li>$2</li>')
            html = html.replace(/\n/g, '<br>')

            return html
        },

        scrollToBottom() {
            this.$nextTick(() => {
                if (this.$refs.chatMessages) {
                    this.$refs.chatMessages.scrollTop = this.$refs.chatMessages.scrollHeight
                }
            })
        },

        saveProfile() {
            if (!this.profileForm.realName) {
                this.$message.warning('请输入真实姓名')
                return
            }

            this.userInfo.realName = this.profileForm.realName
            this.userInfo.email = this.profileForm.email
            this.userInfo.phone = this.profileForm.phone

            localStorage.setItem('user', JSON.stringify(this.userInfo))
            this.$message.success('个人信息保存成功')
        },

        changePassword() {
            if (!this.passwordForm.oldPassword) {
                this.$message.warning('请输入当前密码')
                return
            }
            if (!this.passwordForm.newPassword) {
                this.$message.warning('请输入新密码')
                return
            }
            if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
                this.$message.warning('两次输入的密码不一致')
                return
            }
            if (this.passwordForm.newPassword.length < 6) {
                this.$message.warning('新密码长度不能少于6位')
                return
            }

            this.$message.success('密码修改成功')

            this.passwordForm = {
                oldPassword: '',
                newPassword: '',
                confirmPassword: ''
            }
        }
    }
})
