/**
 * BPS Visualization plugin
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

var wkspId = document.currentScript.getAttribute('workspace_id');

var jsonpath = '/SNA/2/data.json?wid=' + wkspId

console.log("Loaded workspace id " , wkspId);



sigma.parsers.json(jsonpath, {
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

