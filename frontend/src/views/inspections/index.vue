<template>
  <div>
    <LeaderView v-if="isLeaderView" />
    <InspectorView v-else />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../../store/user'
import LeaderView from './LeaderView.vue'
import InspectorView from './InspectorView.vue'

const userStore = useUserStore()

const isLeaderView = computed(() =>
  ['ADMIN', 'UNION_ADMIN', 'CLUB_LEADER'].includes(userStore.role ?? '')
)
</script>
