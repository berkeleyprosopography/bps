<div id="subNavBar">
	<span id="subNavBarLinks">
		{if $currSubNav!='docs'}<a href="/workspace?view=docs">
		{else}<span class="currNav">{/if} 
			Documents{if $currSubNav!='docs'}</a>{else}</span>{/if}
		{if $currSubNav!='people'}<a href="/workspace?view=people">
		{else}<span class="currNav">{/if} 
			People{if $currSubNav!='people'}</a>{else}</span>{/if}
		{if $currSubNav!='clans'}<a href="/workspace?view=clans">
		{else}<span class="currNav">{/if} 
			Clans{if $currSubNav!='clans'}</a>{else}</span>{/if}
		{if $currSubNav!='params'}<a href="/workspace?view=params">
		{else}<span class="currNav">{/if} 
			Settings{if $currSubNav!='params'}</a>{else}</span>{/if}
		{if $currSubNav!='admin'}<a href="/workspace?view=admin">
		{else}<span class="currNav">{/if} 
			Admin{if $currSubNav!='admin'}</a>{else}</span>{/if}
	</span>
</div>

