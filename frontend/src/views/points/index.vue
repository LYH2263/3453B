<template>
  <div>
    <StudentView v-if="isStudentView" />
    <LeaderView v-else-if="isLeaderView" />
    <AdminView v-else />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../../store/user'
import StudentView from './StudentView.vue'
import LeaderView from './LeaderView.vue'
import AdminView from './AdminView.vue'

const userStore = useUserStore()

const isLeaderView = computed(() =>
  userStore.role === 'CLUB_LEADER'
)

const isStudentView = computed(() =>
  userStore.role === 'MEMBER' || userStore.role === 'GUEST'
)
</script>
