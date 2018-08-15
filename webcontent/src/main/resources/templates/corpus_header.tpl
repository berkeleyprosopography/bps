<div id="subNavBar">
	<span id="subNavBarLinks">
		{if $currSubNav!='docs'}<a href="/corpora/corpus?id={$corpusID}&view=docs">
		{else}<span class="currNav">{/if} 
			Documents{if $currSubNav!='docs'}</a>{else}</span>{/if}
		{if $currSubNav!='pnames'}<a href="/corpora/corpus?id={$corpusID}&view=pnames">
		{else}<span class="currNav">{/if} 
			Person-Names{if $currSubNav!='pnames'}</a>{else}</span>{/if}
		{if $currSubNav!='cnames'}<a href="/corpora/corpus?id={$corpusID}&view=cnames">
		{else}<span class="currNav">{/if} 
			Clan-Names{if $currSubNav!='cnames'}</a>{else}</span>{/if}
		{if $canUpdateCorpus != 0 }
			{if $currSubNav!='admin'}<a href="/corpora/corpus?id={$corpusID}&view=admin">
			{else}<span class="currNav">{/if} 
				Admin{if $currSubNav!='admin'}</a>{else}</span>{/if}
		{/if}
	</span>
</div>

