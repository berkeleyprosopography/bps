{include file="header.tpl"}

 <div class="admin_hdr" >
	<p id="orient"><a href="/admin">Admin Main</a> -> Edit User Roles</p>
 </div>

{if isset($perm_error) }
	<h2>{$perm_error}</h2>
{else}
	{if !isset($roles) }
		<p>There are no roles defined!</p>
	{else}
		{if !isset($userroles) }
			<p>There are no user-roles defined!</p>
		{else}
	<table border="0" cellspacing="0" cellpadding="3">
		<tr>
			<td class="title" align="left"><em>User</em></td>
			{section name=role loop=$roles}
				<td class="title" width="100px" align="center">{$roles[role]}</td>
			{/section}
				<td class="title" width="150px" align="center">Workspace</td>
			{foreach key=kuser item=vroles from=$userroles }
				<tr><td class="role" ><p><strong>{$kuser}</strong></p></td>
				{foreach item=role from=$roles}
					<td class="role" align="center">
						<input type="checkbox" class="set" id="{$kuser}.{$role}"
						{if isset($vroles[$role]) }
							checked="true"
						{/if}
						onclick="MarkChanged(event);" />
					</td>
				{/foreach}
				<td class="role" align="center"><p id="w_{$kuser}">
				{if $userworkspaces[$kuser]!=0 }
					<em>Yes</em>&nbsp;&nbsp;
						<input type="button" value="Delete" onclick="deleteWksp('{$kuser}','{$userworkspaces[$kuser]}')" />
				{else}
					<em>No</em>&nbsp;&nbsp;
						<input type="button" value="Add" onclick="addWkspForUser('{$kuser}')" />
				{/if}
					</p>
				</td>
				</tr>
			{/foreach}
		</tr>
	</table>
		
		{/if}
	{/if}
{/if}
{include file="footer.tpl"}
