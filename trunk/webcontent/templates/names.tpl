{if isset($errmsg) }
	<h2>{$errmsg}</h2>
{elseif !isset($corpus) }
	<h2>No corpus specified!</h2>
{else}
	{if empty($names)}
		{if isset($roleFilter)||isset($genderFilter)}
			<h1>No {$type}-Names in this corpus match the selected filters</h1>
			<p>
				<a href="/corpora/corpus?id={$corpusID}&view={$view}">Remove current filters</a>
			</p>
		{else}
			<h1>No {$type}-Name information available for corpus</h1>
		{/if}
	{else}
		<h1>{$names|@count} {$type}-Names in Corpus:</h1>
		<p class="nav-right">
			<span class="filterLabel">Filter by Role:&nbsp;</span>
			<select id="RoleFilterSel">
				<option value="All" {if !isset($roleFilter)}selected="true"{/if}>All Roles</option>
				{section name=irole loop=$roles}
					<option value="{$roles[irole]}" {if $roles[irole]==$roleFilter}selected="true"{/if}>
							{$roles[irole]}</option>
				{/section}
			</select>
			{if $type!='Clan'}
				<span class="filterLabel">Filter by Gender:&nbsp;</span>
				<select id="GenderFilterSel">
					<option value="All" {if !isset($genderFilter)}selected="true"{/if}>
							All Genders</option>
					<option value="male" {if 'male'==$genderFilter}selected="true"{/if}>
							Male</option>
					<option value="female" {if 'female'==$genderFilter}selected="true"{/if}>
							Female</option>
					<option value="unknown" {if 'unknown'==$genderFilter}selected="true"{/if}>
							Unknown</option>
				</select>
			{/if}
			<input id="goBtn" type="button" value="Go"
							onclick="filterNames({$corpusID},'{$orderBy}');" />
		</p>
		<table class="nrads_row" border="0" cellspacing="0" cellpadding="4px" width="100%">
			<tr>
				<td class="title" width="200px">
					{if $orderBy!='name'}<a href="#" 
						onclick="javascript:filterNames({$corpusID},'name');return false;">{/if}
						Name{if $orderBy!='name'}</a>{/if}</td>
				{if $type!='Clan'}
					<td class="title" width="100px">
					{if $orderBy!='gender'}<a href="#" 
						onclick="javascript:filterNames({$corpusID},'gender');return false;">{/if}
						Gender{if $orderBy!='gender'}</a>{/if}</td>
				{/if}
				<td class="title" width="140px">
					{if $orderBy!='docCount'}<a href="#" 
						onclick="javascript:filterNames({$corpusID},'docCount');return false;">{/if}
						# Documents{if $orderBy!='docCount'}</a>{/if}</td>
				<td class="title" width="140px">
					{if $orderBy!='totalCount'}<a href="#" 
						onclick="javascript:filterNames({$corpusID},'totalCount');return false;">{/if}
						Total Instances{if $orderBy!='totalCount'}</a>{/if}</td>
			</tr>
			{section name=iname loop=$names}
				<tr>
					<td class="name" style="padding-top:6px">
						{$names[iname].name}
					</td>
					{if $type!='Clan'}
						<td class="name" style="padding-top:6px">
							{$names[iname].gender}
						</td>
					{/if}
					<td class="name" style="padding-top:6px">
						{$names[iname].docCount}
					</td>
					<td class="name" style="padding-top:6px">
						{$names[iname].totalCount}
					</td>
				</tr>
			{/section}
		</table>
	{/if}
{/if}
