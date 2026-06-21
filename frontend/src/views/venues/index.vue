<template>
  <div>
    <AdminView v-if="isAdminView" />
    <LeaderView v-else-if="isLeaderView" />
    <MemberView v-else />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../../store/user'
import MemberView from './MemberView.vue'
import LeaderView from './LeaderView.vue'
import AdminView from './AdminView.vue'

const userStore = useUserStore()

const isAdminView = computed(() =>
  userStore.role === 'ADMIN' || userStore.role === 'UNION_ADMIN'
)

const isLeaderView = computed(() =>
  userStore.role === 'CLUB_LEADER'
)
</script>
