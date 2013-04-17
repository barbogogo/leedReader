<?php require_once('common.php');

/*
 @nom: json
 @auteur: Barbogogo
 @description: Page de gestion des flus en json en vue d'une utilisation mobile
 */
 
//Récuperation des dossiers de flux par ordre de nom
$folders = $folderManager->populate('name');
//recuperation de tous les flux 
$allFeeds = $feedManager->getFeedsPerFolder();

switch($_REQUEST['option'])
{

    case "article":
    
        $target = "*";
    
        $event = $eventManager->loadAllOnlyColumn($target,array('id' => $_REQUEST['idArticle']));
        
        $content = str_replace("%", "%25", $event[0]->getContent());
        
        echo "{\"content\":", json_encode($content, JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_QUOT | JSON_HEX_AMP | JSON_UNESCAPED_UNICODE), "}\n";
    
        // On met comme lu le event
        $event[0]->change(array('unread'=>'0'),array('id'=>$event[0]->getId()));
        
    break;
    
    case "flux":
        
        $target = "*";
        
        $idFeed = $_REQUEST['feedId'];
        
        $events = $eventManager->loadAllOnlyColumn($target,array('unread'=>1, 'feed'=>$idFeed),'pubDate DESC');
        
        $tab = array();
        $iTab = 0;
        
        foreach($events as $event)
        {
            $tab[$iTab] = array("id" => $event->getId(), 
                                "title" => $event->getTitle(), 
                                "date" => $event->getPubdate("d/m/Y h:i"), 
                                "urlArticle" => $event->getLink(), 
                                "author" => $event->getCreator() );
            
            $iTab ++;
        }
        
        if($iTab == 0)
        {
            $tab[$iTab] = array("id" => "0", "title" => "Pas d'article pour ce flux");
        }
        
        echo "{\"articles\":", json_encode($tab, JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_QUOT | JSON_HEX_AMP | JSON_UNESCAPED_UNICODE), "}\n";
    break;
    
    case "setRead":
        $target = "*";
        $event = $eventManager->loadAllOnlyColumn($target,array('id' => $_REQUEST['idArticle']));
        // On met comme lu le event
        $event[0]->change(array('unread'=>'0'),array('id'=>$event[0]->getId()));
    break;
    
    case "setUnRead":
        $target = "*";
        $event = $eventManager->loadAllOnlyColumn($target,array('id' => $_REQUEST['idArticle']));
        // On met comme non lu le event
        $event[0]->change(array('unread'=>'1'),array('id'=>$event[0]->getId()));
    break;
    
    default:
        $tab = array();
        $iTab = 0;
        
        $nbNoRead = $feedManager->countUnreadEvents();
        
        foreach($folders as $folder)
        {
            $feeds = $allFeeds[$folder->getId()];
            
            foreach($feeds as $title => $value)
            {
                
                foreach($nbNoRead as $title2 => $value2)
                {
                    if($title == $title2)
                    {
                        $allFeeds[$folder->getId()][$title]['nbNoRead'] = $value2;
                    }
                }
            }
            
            $feeds2 = $allFeeds[$folder->getId()];
            
            $tab[$iTab] = array("id" => $folder->getId(), "titre" => $folder->getName(), "flux" => $feeds2);
            
            $iTab ++;
        }

        echo "{\"folders\":", json_encode($tab), "}\n";
    break;
}
?>