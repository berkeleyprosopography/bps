{include file="header.tpl"}
{include file="workspace_header.tpl"}

<div id="vizcontainer" class="vizcontainer">
 
  <div id="graph-container"></div>
	<div id="control-pane">
		<h2 class="underline">Menu</h2>

		<div>
			<h3>min degree <span id="min-degree-val">0</span></h3>
			0 
			<input id="min-degree" type="range" min="0" max="0" value="0">
			<span id="max-degree-value">0</span>
			<br>
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

		<div><input type="checkbox" id="hover-edges-check">Highlight edges on hover</div>
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
	</div> <!-- close control-pane -->

	<div id="data-pane">
		<h2 class="underline">Graph data</h2>
		<div>
			<h3>metadata<div id="metadata">0</div></h3>
			<h3>metrics<div id="metrics">0</div></h3>
		</div>
	</div> <!-- close data-pane -->

	<div id="graph-pane">
		<h2 class="underline">Navigation</h2>
		<div>
			<h3>plot-1<div id="plot-1"></div></h3>
			<h3>plot-2<div id="plot-2"></div></h3>
		</div>
	</div> <!-- close graph-pane -->

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
	</div> <!-- close graph-pane -->

</div> <!-- close vizcontainer -->

<script src="/scripts/sigma/src/sigma.core.js"></script>
<script src="/scripts/sigma/src/conrad.js"></script>
<script src="/scripts/sigma/src/utils/sigma.utils.js"></script>
<script src="/scripts/sigma/src/utils/sigma.polyfills.js"></script>
<script src="/scripts/sigma/src/sigma.settings.js"></script>
<script src="/scripts/sigma/src/classes/sigma.classes.dispatcher.js"></script>
<script src="/scripts/sigma/src/classes/sigma.classes.configurable.js"></script>
<script src="/scripts/sigma/src/classes/sigma.classes.graph.js"></script>
<script src="/scripts/sigma/src/classes/sigma.classes.camera.js"></script>
<script src="/scripts/sigma/src/classes/sigma.classes.quad.js"></script>
<script src="/scripts/sigma/src/classes/sigma.classes.edgequad.js"></script>
<script src="/scripts/sigma/src/captors/sigma.captors.mouse.js"></script>
<script src="/scripts/sigma/src/captors/sigma.captors.touch.js"></script>
<script src="/scripts/sigma/src/renderers/sigma.renderers.canvas.js"></script>
<script src="/scripts/sigma/src/renderers/sigma.renderers.webgl.js"></script>
<script src="/scripts/sigma/src/renderers/sigma.renderers.svg.js"></script>
<script src="/scripts/sigma/src/renderers/sigma.renderers.def.js"></script>
<script src="/scripts/sigma/src/renderers/webgl/sigma.webgl.nodes.def.js"></script>
<script src="/scripts/sigma/src/renderers/webgl/sigma.webgl.nodes.fast.js"></script>
<script src="/scripts/sigma/src/renderers/webgl/sigma.webgl.edges.def.js"></script>
<script src="/scripts/sigma/src/renderers/webgl/sigma.webgl.edges.fast.js"></script>
<script src="/scripts/sigma/src/renderers/webgl/sigma.webgl.edges.arrow.js"></script>
<script src="/scripts/sigma/src/renderers/canvas/sigma.canvas.labels.def.js"></script>
<script src="/scripts/sigma/src/renderers/canvas/sigma.canvas.hovers.def.js"></script>
<script src="/scripts/sigma/src/renderers/canvas/sigma.canvas.nodes.def.js"></script>
<script src="/scripts/sigma/src/renderers/canvas/sigma.canvas.edges.def.js"></script>
<script src="/scripts/sigma/src/renderers/canvas/sigma.canvas.edges.curve.js"></script>
<script src="/scripts/sigma/src/renderers/canvas/sigma.canvas.edges.arrow.js"></script>
<script src="/scripts/sigma/src/renderers/canvas/sigma.canvas.edges.curvedArrow.js"></script>
<script src="/scripts/sigma/src/renderers/canvas/sigma.canvas.edgehovers.def.js"></script>
<script src="/scripts/sigma/src/renderers/canvas/sigma.canvas.edgehovers.curve.js"></script>
<script src="/scripts/sigma/src/renderers/canvas/sigma.canvas.edgehovers.arrow.js"></script>
<script src="/scripts/sigma/src/renderers/canvas/sigma.canvas.edgehovers.curvedArrow.js"></script>
<script src="/scripts/sigma/src/renderers/canvas/sigma.canvas.extremities.def.js"></script>
<script src="/scripts/sigma/src/renderers/svg/sigma.svg.utils.js"></script>
<script src="/scripts/sigma/src/renderers/svg/sigma.svg.nodes.def.js"></script>
<script src="/scripts/sigma/src/renderers/svg/sigma.svg.edges.def.js"></script>
<script src="/scripts/sigma/src/renderers/svg/sigma.svg.edges.curve.js"></script>
<script src="/scripts/sigma/src/renderers/svg/sigma.svg.labels.def.js"></script>
<script src="/scripts/sigma/src/renderers/svg/sigma.svg.hovers.def.js"></script>
<script src="/scripts/sigma/src/middlewares/sigma.middlewares.rescale.js"></script>
<script src="/scripts/sigma/src/middlewares/sigma.middlewares.copy.js"></script>
<script src="/scripts/sigma/src/misc/sigma.misc.animation.js"></script>
<script src="/scripts/sigma/src/misc/sigma.misc.bindEvents.js"></script>
<script src="/scripts/sigma/src/misc/sigma.misc.bindDOMEvents.js"></script>
<script src="/scripts/sigma/src/misc/sigma.misc.drawHovers.js"></script>
<!-- END SIGM/scripts/sigmaIMPORTS -->
<script src="/scripts/sigma/plugins/sigma.parsers.gexf/gexf-parser.js"></script>
<script src="/scripts/sigma/plugins/sigma.parsers.gexf/sigma.parsers.gexf.js"></script>
<script src="/scripts/sigma/plugins/sigma.plugins.filter/sigma.plugins.filter.js"></script>
<script src="/scripts/sigma/plugins/sigma.parsers.json/sigma.parsers.json.js"></script>
<script src="/scripts/sigma/plugins/sigma.layout.forceAtlas2/worker.js"></script>
<script src="/scripts/sigma/plugins/sigma.layout.forceAtlas2/supervisor.js"></script>
<script src="/scripts/sigma/plugins/sigma.renderers.edgeLabels/settings.js"></script>
<script src="/scripts/sigma/plugins/sigma.renderers.edgeLabels/sigma.canvas.edges.labels.def.js"></script>
<script src="/scripts/sigma/plugins/sigma.renderers.edgeLabels/sigma.canvas.edges.labels.curve.js"></script>
<script src="/scripts/sigma/plugins/sigma.plugins.dragNodes/sigma.plugins.dragNodes.js"></script>
<script src="/scripts/sigma/plugins/sigma.renderers.edgeLabels/sigma.canvas.edges.labels.curvedArrow.js"></script>
<script src="/scripts/sigma/plugins/sigma.plugins.animate/sigma.plugins.animate.js"></script>
<script src="/scripts/sigma/plugins/sigma.renderers.snapshot/sigma.renderers.snapshot.js"></script>
<script defer src="/scripts/SNA2/viz.js" workspace_id="{$wkspId}"></script>
<link rel="stylesheet" type="text/css" href="/style/sna2.css"> 

{include file="footer.tpl"}

