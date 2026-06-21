<template>
  <div>
    <LeaderView v-if="isLeaderView" />
    <AdminView v-else />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../store/user'
import LeaderView from './LeaderView.vue'
import AdminView from './AdminView.vue'

const userStore = useUserStore()

const isLeaderView = computed(() =>
  userStore.role === 'CLUB_LEADER'
)
</script>
