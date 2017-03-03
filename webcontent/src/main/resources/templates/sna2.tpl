

<div id="container" class="container">
 
  <div id="graph-container"></div>
  <div id="control-pane">
    <h2 class="underline">Menu</h2>
    
    <div>
      <h3>min degree <span id="min-degree-val">0</span></h3>
      0 <input id="min-degree" type="range" min="0" max="0" value="0"> <span id="max-degree-value">0</span><br>
    </div>
    <div>
      <h3>node category (gender)</h3>
      <select id="node-category">
        <option value="" selected>All categories</option>
      </select>
      <h3>Edge category (contract type)</h3>
      <select id="edge-category">
        <option value="" selected>All categories</option>
      </select>
    </div>
    <span class="line"></span>
    <div><input type="checkbox" id="hover-edges-check"> Highlight edges on hover</div>
    <div><input type="checkbox" id="animate-check">Animate</div>
    <div><input type="checkbox" id="round-layout-check">Round layout</div>
    <div><input type="checkbox" id="hide-edges-check">Toggle edges</div>
    <div><input type="checkbox" id="hide-nodenames-check">Toggle node names</div>
    <span class="line"></span>
    <div>
      <button id="reset-btn">Reset filters</button>
      <button id="export-btn">Dump</button>
      <button id="image-export-button">Export</button>
      
    </div>
    
    <div id="dump" class="hidden"></div>
  </div>

   <div id="data-pane">
    <h2 class="underline">Graph data</h2>
    
    <div>
      <h3>metadata<div id="metadata">0</div></h3>
      <h3>metrics<div id="metrics">0</div></h3>
    </div>

  </div>
   <div id="graph-pane">
    <h2 class="underline">Navigation</h2>
    
    <div>

      <h3>plot-1<div id="plot-1"></div></h3>
      <h3>plot-2<div id="plot-2"></div></h3>
    </div>

  </div>
   <div class="direction-panel">
        <div class="move-up">
            <button type="button">&#8593;</button>
        </div>
        <div class="move-down">
            <button type="button">&#8595;</button>
        </div>
        <div class="move-left">
            <button type="button">&#8592;</button>
        </div>
        <div class="move-right">
            <button type="button">&#8594;</button>
        </div>
</div>
</div>
<script src="https://cdn.jsdelivr.net/g/sigma.js@1.0.2(sigma.min.js+plugins/sigma.layout.forceAtlas2.min.js+plugins/sigma.parsers.gexf.min.js+plugins/sigma.parsers.json.min.js+plugins/sigma.plugins.animate.min.js+plugins/sigma.plugins.dragNodes.min.js+plugins/sigma.plugins.neighborhoods.min.js+plugins/sigma.renderers.customShapes.min.js)"></script>
<script defer src="/scripts/SNA2/viz.js" workspace_id="{$wkspId}"></script>
<link rel="stylesheet" type="text/css" href="/style/sna2.css"> 

</script>

{include file="footer.tpl"}

