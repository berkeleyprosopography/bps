{include file="header.tpl"}

 
<div id="container">
  <style>
    body {
      color: #333;
      font-size: 14px;
      font-family: Lato, sans-serif;
    }
    #graph-container {
      top: 0;
      bottom: 0;
      left: 0;
      right: 0;
      position: absolute;
    }
    /* Control panel: */
.control-panel {
  color: #666;
  border-top: 1px #ccc dashed;
  background: #fff;
  position: relative;
  height: 84px;
}
.move-up {
  position: absolute;
  width: 72px;
  height: 72px;
  top: 0;
  right: 60px;
  margin: 5px;
}
.move-down {
  position: absolute;
  width: 72px;
  height: 72px;
  top: 20px;
  right: 60px;
  margin: 5px;
}
.move-left {
  position: absolute;
  width: 72px;
  height: 72px;
  top: 10px;
  right: 90px;
  margin: 5px;
}
.move-right {
  position: absolute;
  width: 72px;
  height: 72px;
  top: 10px;
  right: 35px;
  margin: 5px;
}
    #data-pane {
      top: 10px;
      /*bottom: 10px;*/
      left: 10px;
      position: absolute;
      width: 230px;
      background-color: rgb(249, 247, 237);
      box-shadow: 0 2px 6px rgba(0,0,0,0.3);
    }
    #graph-pane {
      bottom: 10px;
      /*bottom: 10px;*/
      left: 10px;
      position: absolute;
      width: 230px;
      height: 130px;
      background-color: rgb(249, 247, 237);
      box-shadow: 0 2px 6px rgba(0,0,0,0.3);
    }
    #control-pane > div {
      margin: 10px;
      overflow-x: auto;
    }
    #control-pane {
      top: 10px;
      /*bottom: 10px;*/
      right: 10px;
      position: absolute;
      width: 230px;
      background-color: rgb(249, 247, 237);
      box-shadow: 0 2px 6px rgba(0,0,0,0.3);
    }
    #control-pane > div {
      margin: 10px;
      overflow-x: auto;
    }
    .line {
      clear: both;
      display: block;
      width: 100%;
      margin: 0;
      padding: 12px 0 0 0;
      border-bottom: 1px solid #aac789;
      background: transparent;
    }
    h2, h3, h4 {
      padding: 0;
      font-variant: small-caps;
    }
    .green {
      color: #437356;
    }
    h2.underline {
      color: #437356;
      background: #f4f0e4;
      margin: 0;
      border-radius: 2px;
      padding: 8px 12px;
      font-weight: 700;
    }
    .hidden {
      display: none;
      visibility: hidden;
    }

    input[type=range] {
      width: 160px;
    }
  </style>
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
<script>
/**
 * This is an example on how to use sigma filters plugin on a real-world graph.
 */
var filter;


var stringToColour = function(str) {
  var hash = 0;
  for (var i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  var colour = '#';
  for (var i = 0; i < 3; i++) {
    var value = (hash >> (i * 8)) & 0xFF;
    colour += ('00' + value.toString(16)).substr(-2);
  }
  console.log('stringToColour', str, '->', colour)
  return colour;
};
/**
 * DOM utility functions
 */
var _ = {
  $: function (id) {
    return document.getElementById(id);
  },

  all: function (selectors) {
    return document.querySelectorAll(selectors);
  },

  removeClass: function(selectors, cssClass) {
    console.log("removeClass");
    var nodes = document.querySelectorAll(selectors);
    var l = nodes.length;
    for ( i = 0 ; i < l; i++ ) {
      var el = nodes[i];
      // Bootstrap compatibility
      el.className = el.className.replace(cssClass, '');
    }
  },

  addClass: function (selectors, cssClass) {
    console.log("addClass");
    var nodes = document.querySelectorAll(selectors);
    var l = nodes.length;
    for ( i = 0 ; i < l; i++ ) {
      var el = nodes[i];
      // Bootstrap compatibility
      if (-1 == el.className.indexOf(cssClass)) {
        el.className += ' ' + cssClass;
      }
    }
  },

  show: function (selectors) {
    console.log("show");
    this.removeClass(selectors, 'hidden');
  },

  hide: function (selectors) {
    console.log("hide");
    this.addClass(selectors, 'hidden');
  },

  toggle: function (selectors, cssClass) {
    console.log("toggle");
    var cssClass = cssClass || "hidden";
    var nodes = document.querySelectorAll(selectors);
    var l = nodes.length;
    for ( i = 0 ; i < l; i++ ) {
      var el = nodes[i];
      //el.style.display = (el.style.display != 'none' ? 'none' : '' );
      // Bootstrap compatibility
      if (-1 !== el.className.indexOf(cssClass)) {
        el.className = el.className.replace(cssClass, '');
      } else {
        el.className += ' ' + cssClass;
      }
    }
  }
};


function updatePane (graph, filter) {
  console.log("updatePane");
  // get max degree
  var maxDegree = 0,
      categories = {};
      edge_categories = {};
      edge_document = {};

  // read nodes
  graph.nodes().forEach(function(n) {
    console.log("updatePane:: nodes:: forEach", n);
    maxDegree = Math.max(maxDegree, graph.degree(n.id));
    categories[n.gender] = true;
    console.log("updatePane:: nodes:: forEach:: maxDegree", maxDegree);
  })

  // min degree
  _.$('min-degree').max = maxDegree;
  _.$('max-degree-value').textContent = maxDegree;
  
  // node category
  var nodecategoryElt = _.$('node-category');
  Object.keys(categories).forEach(function(c) {
    var optionElt = document.createElement("option");
    optionElt.text = c;
    nodecategoryElt.add(optionElt);
  });



    // read nodes
  graph.edges().forEach(function(n) {
    console.log("updatePane:: edges:: forEach", n);
    edge_categories[n.label] = true;
    console.log("updatePane:: edge:: forEach:: found", n.label);
  })

  var edgecategoryElt = _.$('edge-category');
  Object.keys(edge_categories).forEach(function(c) {
    var optionElt = document.createElement("option");
    optionElt.text = c;
    edgecategoryElt.add(optionElt);
  });

  // reset button
  _.$('reset-btn').addEventListener("click", function(e) {
    _.$('min-degree').value = 0;
    _.$('min-degree-val').textContent = '0';
    _.$('node-category').selectedIndex = 0;
    _.$('edge-category').selectedIndex = 0;
    filter.undo().apply();
    _.$('dump').textContent = '';
    _.hide('#dump');
  });

  // export button
  _.$('export-btn').addEventListener("click", function(e) {
    var chain = filter.export();
    console.log(chain);
    _.$('dump').textContent = JSON.stringify(chain);
    _.show('#dump');
  });
}

console.log("init parser");

sigma.parsers.json('/SNA/2/data.json?wid={$wkspId}', {
  //container: 'graph-container',
  renderer: {
    container: document.getElementById('graph-container'),
    type: 'canvas'
  },
  settings: {
    edgeLabelSize: 'proportional',
    doubleClickEnabled: false,
    //minEdgeSize: 0.5,
    //maxEdgeSize: 4,
    enableEdgeHovering: true,
    edgeHoverColor: 'red',
    defaultEdgeHoverColor: '#f02',

    edgeHoverSizeRatio: 3,
    edgeHoverExtremities: true,
  }
}, function(s) {
  // Initialize the Filter API

  console.log("edit parsed graph for visualization");
 var i,
  nodes = s.graph.nodes(),
  len = nodes.length;


for (i = 0; i < len; i++) {
    nodes[i].x = 0.35* Math.log(s.graph.degree(nodes[i].id));
    nodes[i].y = (Math.random() * 0.75)  ;//* ( 0.25 * Math.log(nodes[i].BetweennessCentrality));
    nodes[i].size = 5// s.graph.degree(nodes[i].id);
    nodes[i].label = nodes[i].name;
    nodes[i].color = nodes[i].center ? '#333' : '#666';
    console.log('Node', nodes[i], 'has x,y =', nodes[i].x, nodes[i].y, "and size",nodes[i].size );
}

var k,
  edges = s.graph.edges(),
  len = edges.length;

console.log('Iterating over edges...');
for (k = 0; k < len; k++) {
    edges[k].label = edges[k].type;
    edges[k].size = parseFloat(edges[k].weight); //have weights for relationships! these are important
    edges[k].type = 'curvedArrow';
    edges[k].color = stringToColour(edges[k].label);
    console.log('Edge', k, 'is', edges[k] );
    //type: ['line', 'curve', 'arrow', 'curvedArrow'][Math.random() * 4 | 0]

}
console.log("refreshing");
// Refresh the display:
s.refresh();

console.log(s);
// ForceAtlas Layout




console.log("adding filters");

  console.log(s);
  filter = new sigma.plugins.filter(s);
console.log("updating pane with filer");
  updatePane(s.graph, filter);

  function applyMinDegreeFilter(e) {
    console.log("applyMinDegreeFilter");
    var v = e.target.value;
    _.$('min-degree-val').textContent = v;

    filter
      .undo('min-degree')
      .nodesBy(function(n) {
        return this.degree(n.id) >= v;
      }, 'min-degree')
      .apply();
  }

  function applyCategoryFilter(e) {
    console.log("applyCategoryFilter");
    var c = e.target[e.target.selectedIndex].value;
    filter
      .undo('node-category')
      .nodesBy(function(n) {
        return !c.length || n.gender === c;
      }, 'node-category')
      .apply();
  }

  function applyEdgeCategoryFilter(e) {
    console.log("applyEdgeCategoryFilter");
    var c = e.target[e.target.selectedIndex].value;
    filter
      .undo('edge-category')
      .edgesBy(function(n) {
        var retval = !c.length || n.label === c;
        
        nodes.filter(function(nn) { 
          if ( nn.id === n.source || nn.id === n.target){
            console.log('DISPLAY', nn.id);
          }
          else{
            console.log('HIDE', nn.id);
          }

           })

        return retval;

      }, 'edge-category')

      .apply();

  }

function toggleRoundLayout(e) {
    console.log("toggleRoundLayout", e.target.checked);
    L = 10,
    N = 100;
    if (e.target.checked == true) {
          console.log('animating grid layout')
          //s.killForceAtlas2();   
        }
     else {
       console.log(' not animating grid layout')
     }
  }

 function animate(e) {
    console.log("animate", e.target.checked);
    if (e.target.checked == true) {
        s.startForceAtlas2({worker: true, barnesHutOptimize: false});
    }
    else {
      s.killForceAtlas2();
    }

  }


 function hideEdges(e) {
    console.log("hideEdges", e.target.checked);
    if (e.target.checked == true) {
        s.settings({drawEdges: false,});
        s.refresh();
    }
    else {
      s.settings({drawEdges: true,});
      s.refresh();
    }

  }

 function hideNodenames(e) {
    console.log("hideEdges", e.target.checked);
    if (e.target.checked == true) {
        s.settings({drawLabels: false,});
        s.refresh();
    }
    else {
      s.settings({drawLabels: true,});
      s.refresh();
    }

  }

s.bind('overEdge', function (e) {
  
  _.$('metadata').innerHTML = JSON.stringify(e.data.edge, null, 1);
});

s.bind('outEdge', function (e) {

  _.$('metadata').innerHTML = '';
});


s.bind('overNode', function (e) {
  var k,
  edges = s.graph.edges(),
  len = edges.length;
  d = e.data.node.id
  _.$('metadata').innerHTML = JSON.stringify(e.data.node, null, 1);

  calculate = false;
  
  if (_.$('hover-edges-check').checked == true){
    calculate = true;
  }
 
  if (calculate == true) {
  console.log('Looking for adjacent edges', calculate);
  for (k = 0; k < len; k++) {
    
    //console.log("edge", k, 'from', edges[k].source, 'to', edges[k].target, 'in', d , d != edges[k].source && d != edges[k].target);
    if (d == edges[k].source || d == edges[k].target) {
        console.log('found edge', edges[k]);
        edges[k].color = '#00f';
    }
    else {
      //console.log("excluding", edges[k]);

    }
  }
  s.refresh();
}
});


s.bind('outNode', function (e) {

  _.$('metadata').innerHTML = '';


  calculate = false;
  if (_.$('hover-edges-check').checked == true){
    calculate = true;
  }
 
  if (calculate == true){
    var k,
    edges = s.graph.edges(),
    len = edges.length;

    console.log('resetting edges color');
    for (k = 0; k < len; k++) {
          edges[k].color = stringToColour(edges[k].label);
    }
    s.refresh();
}
});

function onNodeHover(event) {
    window.console.log("clicked!");
} 


function exportAsPng(){
  s.renderers[0].snapshot({format: 'png', background: 'white', filename: 'graph.png',});
}

var dragListener = sigma.plugins.dragNodes(s, s.renderers[0]);
console.log("Registering drag listeners", dragListener);
dragListener.bind('startdrag', function(event) {
  console.log(event);
});
dragListener.bind('drag', function(event) {
  console.log(event);
});
dragListener.bind('drop', function(event) {
  console.log(event);
});
dragListener.bind('dragend', function(event) {
  console.log(event);
});

console.log("Registering filter listeners");
  _.$('min-degree').addEventListener("input", applyMinDegreeFilter);  // for Chrome and FF
  _.$('min-degree').addEventListener("change", applyMinDegreeFilter); // for IE10+, that sucks
  _.$('node-category').addEventListener("change", applyCategoryFilter);
  _.$('edge-category').addEventListener("change", applyEdgeCategoryFilter);
  _.$('animate-check').addEventListener("change", animate);
  _.$('round-layout-check').addEventListener("change", toggleRoundLayout);
  _.$('image-export-button').addEventListener("click", exportAsPng);
  _.$('hide-edges-check').addEventListener("change", hideEdges);
  _.$('hide-nodenames-check').addEventListener("change", hideNodenames);
});

// Initialize the dragNodes plugin:


</script>

{include file="footer.tpl"}
