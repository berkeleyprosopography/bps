<div id="subNavBar">
	<span id="subNavBarLinks">
		{if $currSubNav!='docs'}<a href="/workspace?view=docs&wid={$wkspId}">
		{else}<span class="currNav">{/if} 
			Documents{if $currSubNav!='docs'}</a>{else}</span>{/if}
		{if $currSubNav!='people'}<a href="/workspace?view=people&wid={$wkspId}">
		{else}<span class="currNav">{/if} 
			People{if $currSubNav!='people'}</a>{else}</span>{/if}
		{if $currSubNav!='clans'}<a href="/workspace?view=clans&wid={$wkspId}">
		{else}<span class="currNav">{/if} 
			Clans{if $currSubNav!='clans'}</a>{else}</span>{/if}
		{if $currSubNav!='params'}<a href="/workspace?view=params&wid={$wkspId}">
		{else}<span class="currNav">{/if} 
			Settings{if $currSubNav!='params'}</a>{else}</span>{/if}
		{if $currSubNav!='admin'}<a href="/workspace?view=admin&wid={$wkspId}">
		{else}<span class="currNav">{/if} 
			Admin{if $currSubNav!='admin'}</a>{else}</span>{/if}
		<a href="/SNA?wid={$wkspId}">SNA</a>
	</span>
</div>

