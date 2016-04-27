{include file="header.tpl"}

  <div id="container" class="container">
	<header>
	<h1 id='forcedirected_graph'>SNA Visualizer</h1>
    </header>

      <div id="song_selection" class="control">
        <h3>Views</h3>
        <select id="source_select">
          <option value="data.json?wid={$wkspId}">Corpus</option>
          <option value="data.json?raw=True&wid={$wkspId}">Corpus (RAW)</option>
        </select>
      </div>
    <div id="controls">
      <div id="layouts" class="control">
        <h3>Layout</h3>
        <a id="force" class="active">Force Directed</a>
        <a id="radial">Radial</a>
      </div>
      <div id="filters" class="control">
        <h3>Filter (degree)</h3>
        <a id="all" class="active">All</a>
        <a id="popular">Popular</a>
        <a id="obscure">Peripheral</a>
      </div>
      <div id="sorts" class="control">
        <h3>Sort</h3>
        <a id="cluster" class="active">Cluster</a>
        <a id="degree">Degree</a>
      </div>
      <div id="search_section" class="control">
        <form id="search_form" action=""  method="post">
          <p class="search_title">Search <input type="text" class="text-input" id="search" value="" /></p>
        </form>
      </div>
<div><br></div>
    <div id="main" role="main">
      <div id="vis"></div>
    </div>

	<div id="bottom_controls" >
		<div id="force" class="control">
			<h3>Zoom</h3>
			<input id="force_slider" type="range" min="0" max="2000" value="200" />
			<a id="ForceStart" class="active">Active</a>
			<a id="ForceStop" >Paused</a>
		</div>
		
		<div id="edgeshow"  class="control">
			<h3>Edges</h3>
			<a id="EdgesOn" class="active">Show</a>
			<a id="EdgesOff" >Hide</a>
		</div>
	
		<div id="nodenames"  class="control">
			<h3>Node names</h3>
			<a id="NodeNamesOn" class="active">Show</a>
			<a id="NodeNamesOff" >Hide</a>
		</div>
		
  <div id="nodenames"  class="control">
      <h3>Static views</h3>
      <a href="/SNA/tree">Family Tree</a>
    </div>
	
	</div>
  </div>


  <script src="https://code.jquery.com/jquery-1.12.3.js"></script>
	<script defer src="/scripts/SNA/libs/plugins.js"></script>
  <script defer src="/scripts/SNA/force.js"></script>
  <script src="/scripts/SNA/Tooltip.js"></script>
  <script src="/scripts/SNA/fisheye.js"></script>

 

  </body>
</html>

{include file="footer.tpl"}
