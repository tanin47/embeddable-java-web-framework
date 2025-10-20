<script lang="ts">
import Button from './_button.svelte'

let title = "Embeddable Java Web Framework.";
let description = "Zero Dependencies. Small footprint (500K). Support Typescripts and Svelte. Great for embedding a website on other large systems.";
</script>

<div class="container mx-auto p-8 flex flex-col gap-2">
  <div class="flex items-center gap-4 mb-4">
    <img src="/assets/images/logo.png" class="h-[32px]"/>
    <h1 class="text-2xl font-bold">{title}</h1>
  </div>
  <p>{description}</p>
  <Button />
</div>

<style lang="scss">
</style>
