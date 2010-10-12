INSERT INTO role( id, name, wksp_role, description, creation_time ) VALUES 
   ( 1, 'Admin', false, 'Manages users, roles, permissions and other aspects of the system.', now() ),
   ( 2, 'WorkspaceOwner', true, 'Owner/admin of a workspace. Has all rights over workspace, and can grant others rights on workspace.', now() ),
   ( 3, 'Viewer', true, 'An authenticated user of the system with workspace read rights.', now() ),
   ( 4, 'Collaborator', true, 'An authenticated user of the system with workspace read and update rights.', now() ),
   ( 5, 'Corpus Admin', false, 'Manages the corpora in the system, adding, updating and removing corpora for use in workspaces.', now() );

INSERT INTO permission( id, name, description, creation_time ) VALUES 
   ( 1, 'EditRoles', 'Permission to add new roles, delete existing roles and to change the permissions associated with existing roles. Permission to edit comments associated with existing roles.', now() ),
   ( 2, 'EditPerms', 'Permission to add new permissons and delete existing permissions. Permission to edit comments associated with existing permissions.', now() ),
   ( 3, 'AssignRoles', 'Permission to set roles for a user.', now() ),
   ( 4, 'AssignWorkspaceRoles', 'Permission to set roles on a workspace for a user.', now() ),
   ( 5, 'WorkspaceRead', 'Permission to view information from a workspace.', now() ),
   ( 6, 'WorkspaceUpdate', 'Permission to view information from a workspace.', now() ),
   ( 7, 'WorkspaceCreate', 'Permission to create new workspaces.', now() ),
   ( 8, 'WorkspaceDelete', 'Permission to delete workspaces.', now() ),
   ( 9, 'CorpusAdd', 'Permission to add new corpora.', now() ),
   ( 10, 'CorpusUpdate', 'Permission to modify corpora.', now() ),
   ( 11, 'CorpusDelete', 'Permission to delete corpora.', now() );

INSERT INTO role_perms( role_id, perm_id, creation_time ) VALUES 
   ( 1, 1, now() ),
   ( 1, 2, now() ),
   ( 1, 3, now() ),
   ( 1, 4, now() ),
   ( 1, 5, now() ),
   ( 1, 6, now() ),
   ( 1, 7, now() ),
   ( 1, 8, now() ),
   ( 2, 4, now() ),
   ( 2, 5, now() ),
   ( 2, 6, now() ),
   ( 3, 5, now() ),
   ( 4, 5, now() ),
   ( 4, 6, now() ),
   ( 5, 9, now() ),
   ( 5, 10, now() ),
   ( 5, 11, now() );

INSERT INTO user( id, username, passwdmd5, email, pending, creation_time ) VALUES
( 1, 'admin', md5('bps4me'), 'pschmitz@berkeley.edu', 0, now() );

INSERT INTO user_roles( user_id, role_id, workspace_id, creation_time ) VALUES
( 1, 1, -1, now() );
