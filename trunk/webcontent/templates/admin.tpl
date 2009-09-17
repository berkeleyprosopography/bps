{include file="header.tpl"}
 <div class="admin_hdr" >
	<p id="orient">Admin Main</p>
 </div>

<dl>
<dt><h3><a href="../auth/register.php">Register a new user</a></h3></dt>
<dd>Allows you to create a new user in the system, set their password and provide an email address for contact.</dd>
<dt><h3><a href="adminUserRoles.php">Assign User Roles</a></h3></dt>
<dd>Sets up the roles for users, which in turn grants them
permissions associated with each role.</dd>
{if $currentUser_isAdmin }
<dt><h3><a href="adminRoles.php">Edit Role Definitions</a></h3></dt>
<dd>Define new roles to group permissions.</dd>
<dt><h3><a href="adminRolePerms.php">Edit Role Permissions</a></h3></dt>
<dd>Add permissions to a role.</dd>
<dt><h3><a href="adminPermissions.php">Edit Permission Definitions</a></h3></dt>
<dd>Creates new permissions (only for developers).</dd>
{/if}
<dt><h3><a href="editNews.php?id=1">Edit News Content</a></h3></dt>
<dd>Creates News items for display on the front page.</dd>
</dl>
<p></p>
<p></p>

{include file="footer.tpl"}
