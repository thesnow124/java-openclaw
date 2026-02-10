<template>
  <div class="agents">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px">
      <h2>Agent Management</h2>
      <el-button type="primary" @click="createAgent">
        <el-icon><Plus /></el-icon>
        New Agent
      </el-button>
    </div>
    
    <el-table :data="[]" style="margin-top: 20px">
      <el-table-column prop="id" label="Agent ID" width="150" />
      <el-table-column prop="name" label="Name" width="200" />
      <el-table-column prop="model" label="Model" width="150" />
      <el-table-column prop="workspace" label="Workspace" />
      <el-table-column prop="createdAt" label="Created" width="180" />
      <el-table-column label="Actions" width="200">
        <template #default="{ row }">
          <el-button size="small" type="primary">Edit</el-button>
          <el-button size="small" type="warning">Files</el-button>
          <el-button size="small" type="danger">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
function createAgent() {
  console.log('Create new agent')
  // TODO: Open create agent dialog
}
</script>

<style scoped>
.agents {
  padding: 20px;
}
</style>
