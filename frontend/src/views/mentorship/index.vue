<template>
  <div>
    <LeaderView v-if="isLeaderView" />
    <MentorView v-else-if="isMentorView" />
    <StudentView v-else />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../../store/user'
import StudentView from './StudentView.vue'
import LeaderView from './LeaderView.vue'
import MentorView from './MentorView.vue'

const userStore = useUserStore()

const isLeaderView = computed(() =>
  userStore.role === 'ADMIN' || userStore.role === 'UNION_ADMIN' || userStore.role === 'CLUB_LEADER'
)

const isMentorView = computed(() => {
  return false
})
</script>
